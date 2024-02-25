package com.evanisnor.flowmeter.features.flowtimesession

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionComplete
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent.EndBreak
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent.EndSession
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent.NewSession
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent.TakeABreak
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionInProgress
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.StartNew
import com.evanisnor.flowmeter.features.flowtimesession.domain.AttentionGrabber
import com.evanisnor.flowmeter.features.flowtimesession.domain.FlowTimeSession
import com.evanisnor.flowmeter.features.flowtimesession.domain.NoOpFlowTimeSession
import com.evanisnor.flowmeter.features.flowtimesession.domain.TimeFormatter
import com.evanisnor.flowmeter.system.NotificationSystem
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class SessionContentPresenter @Inject constructor(
  private val flowTimeSessionProvider: Provider<FlowTimeSession>,
  private val timeFormatter: TimeFormatter,
  private val attentionGrabber: AttentionGrabber,
  private val notificationSystem: NotificationSystem,
) : Presenter<SessionContent> {

  @Composable
  override fun present(): SessionContent {
    val scope = rememberCoroutineScope()
    val session = rememberRetained { mutableStateOf<FlowTimeSession>(NoOpFlowTimeSession) }
    val takingABreak = rememberRetained { mutableStateOf(false) }
    val breakRecommendation = rememberRetained { mutableStateOf(0.minutes) }
    val notifyBreakIsOver = rememberRetained { mutableStateOf(false) }

    LaunchedEffect(session.value, notifyBreakIsOver.value) {
      if (notifyBreakIsOver.value) {
        attentionGrabber.notifyBreakIsOver()
      } else {
        attentionGrabber.clearBreakIsOverNotification()
      }
    }

    val eventSink: (SessionEvent) -> Unit = { event ->
      when (event) {
        is NewSession -> {
          Timber.i("User requested new FlowTime session")
          checkForNotificationPermission()
          takingABreak.value = false
          notifyBreakIsOver.value = false
          session.value.stop()
          session.value = flowTimeSessionProvider.get()
          scope.launch {
            attentionGrabber.notifySessionStarted()
          }
        }
        is EndSession -> {
          Timber.i("User wants session to stop")
          session.value.stop()
        }
        is TakeABreak -> {
          Timber.i("User wants to take a break")
          takingABreak.value = true
          notifyBreakIsOver.value = false
          breakRecommendation.value = event.duration
          session.value = flowTimeSessionProvider.get()
        }
        is EndBreak -> {
          Timber.i("User wants break time to end")
          session.value.stop()
          breakRecommendation.value = 0.minutes
        }
      }
    }

    val sessionContent by produceRetainedState<SessionContent>(initialValue = StartNew(eventSink)) {
      snapshotFlow { session.value }.collectLatest { session ->
        session.collect { flowTimeState ->
          value = if (takingABreak.value) {
            flowTimeState.toBreakContent(breakRecommendation.value, eventSink).also {
              notifyBreakIsOver.value =
                (it is SessionContent.TakingABreak && it.isBreakLongerThanRecommended)
            }
          } else {
            flowTimeState.toSessionContent(eventSink)
          }
        }
      }
    }

    return sessionContent
  }

  private fun checkForNotificationPermission() {
    if (!notificationSystem.isNotificationPermissionGranted()) {
      notificationSystem.requestPermission()
    }
  }

  private fun FlowTimeSession.State.toSessionContent(eventSink: (SessionEvent) -> Unit) =
    when (this) {
      is FlowTimeSession.State.Tick ->
        SessionInProgress(
          duration = timeFormatter.humanReadableClock(duration),
          eventSink = eventSink,
        )
      is FlowTimeSession.State.Complete ->
        SessionComplete(
          duration = timeFormatter.humanReadableSentence(sessionDuration),
          breakRecommendation = recommendedBreak,
          eventSink = eventSink,
        )
    }

  private fun FlowTimeSession.State.toBreakContent(
    breakRecommendation: Duration,
    eventSink: (SessionEvent) -> Unit,
  ) =
    when (this) {
      is FlowTimeSession.State.Tick ->
        SessionContent.TakingABreak(
          duration = timeFormatter.humanReadableClock(duration),
          breakRecommendation = breakRecommendation,
          isBreakLongerThanRecommended = duration >= breakRecommendation,
          eventSink = eventSink,
        )
      is FlowTimeSession.State.Complete -> StartNew(eventSink = eventSink)
    }

}
