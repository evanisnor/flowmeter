package com.evanisnor.flowmeter.features.flowtimesession

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import com.evanisnor.flowmeter.features.flowtimesession.domain.FlowTimeSession
import com.evanisnor.flowmeter.features.flowtimesession.domain.NoOpFlowTimeSession
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent.EndSession
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent.NewSession
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionComplete
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionInProgress
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.StartNew
import com.evanisnor.flowmeter.features.flowtimesession.domain.TimeFormatter
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import javax.inject.Provider

class SessionContentPresenter @Inject constructor(
  private val flowTimeSessionProvider: Provider<FlowTimeSession>,
  private val timeFormatter: TimeFormatter,
) : Presenter<SessionContent> {

  @Composable
  override fun present() : SessionContent {
    val session = rememberRetained { mutableStateOf<FlowTimeSession>(NoOpFlowTimeSession) }

    val eventSink: (SessionEvent) -> Unit = { event ->
      when (event) {
        is NewSession -> session.value = flowTimeSessionProvider.get()
        is EndSession -> session.value.stop()
      }
    }

    val sessionContent by produceRetainedState<SessionContent>(initialValue = StartNew(eventSink)) {
      snapshotFlow { session.value }.collectLatest {
        it.collect { flowTimeState ->
          value = flowTimeState.toContent(eventSink)
        }
      }
    }

    return sessionContent
  }

  private fun FlowTimeSession.State.toContent(eventSink: (SessionEvent) -> Unit) =
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

}
