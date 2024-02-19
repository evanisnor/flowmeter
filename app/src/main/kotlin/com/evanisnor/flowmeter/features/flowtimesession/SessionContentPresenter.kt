package com.evanisnor.flowmeter.features.flowtimesession

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionComplete
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent.EndBreak
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent.EndSession
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent.NewSession
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent.TakeABreak
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionInProgress
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.StartNew
import com.evanisnor.flowmeter.features.flowtimesession.domain.FlowTimeSession
import com.evanisnor.flowmeter.features.flowtimesession.domain.NoOpFlowTimeSession
import com.evanisnor.flowmeter.features.flowtimesession.domain.TimeFormatter
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import javax.inject.Provider
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class SessionContentPresenter @Inject constructor(
  private val flowTimeSessionProvider: Provider<FlowTimeSession>,
  private val timeFormatter: TimeFormatter,
) : Presenter<SessionContent> {

  @Composable
  override fun present(): SessionContent {
    val session = rememberRetained { mutableStateOf<FlowTimeSession>(NoOpFlowTimeSession) }
    val takingABreak = rememberRetained { mutableStateOf(false) }
    val breakRecommendation = rememberRetained { mutableStateOf(0.minutes) }

    val eventSink: (SessionEvent) -> Unit = { event ->
      when (event) {
        is NewSession -> {
          takingABreak.value = false
          session.value = flowTimeSessionProvider.get()
        }
        is EndSession -> {
          session.value.stop()
        }
        is TakeABreak -> {
          takingABreak.value = true
          breakRecommendation.value = event.duration
          session.value = flowTimeSessionProvider.get()
        }
        is EndBreak -> {
          breakRecommendation.value = 0.minutes
          session.value.stop()
        }
      }
    }

    val sessionContent by produceRetainedState<SessionContent>(initialValue = StartNew(eventSink)) {
      snapshotFlow { session.value }.collectLatest {
        it.collect { flowTimeState ->
          value = if (takingABreak.value) {
            flowTimeState.toBreakContent(breakRecommendation.value, eventSink)
          } else {
            flowTimeState.toSessionContent(eventSink)
          }
        }
      }
    }

    return sessionContent
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
          isBreakLongerThanRecommended = duration > breakRecommendation,
          eventSink = eventSink,
        )
      is FlowTimeSession.State.Complete -> StartNew(eventSink = eventSink)
    }

}