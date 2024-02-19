package com.evanisnor.flowmeter.features.flowsession.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.Event
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.State.Content.SessionEvent.EndSession
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.State.Content.SessionEvent.NewSession
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.State
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.State.Content.SessionEvent
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.State.Content.SessionComplete
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.State.Content.SessionInProgress
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.State.Content.StartNew
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import javax.inject.Provider

@CircuitInject(FlowTimeScreen::class, AppScope::class)
class FlowTimePresenter @Inject constructor(
  private val flowTimeSessionProvider: Provider<FlowTimeSession>,
) : Presenter<State> {

  @Composable
  override fun present(): State {
    val session = rememberRetained { mutableStateOf<FlowTimeSession>(NoOpFlowTimeSession) }

    val eventSink: (Event) -> Unit = { _ -> }
    val sessionEventSink: (SessionEvent) -> Unit = { event ->
      when (event) {
        is NewSession -> session.value = flowTimeSessionProvider.get()
        is EndSession -> session.value.stop()
      }
    }

    val content by produceRetainedState<State.Content>(initialValue = StartNew(sessionEventSink)) {
      snapshotFlow { session.value }.collectLatest {
        it.collectLatest { flowTimeState ->
          value = when (flowTimeState) {
            is FlowTimeSession.State.Tick ->
              SessionInProgress(
                duration = flowTimeState.duration.inWholeSeconds.toString(),
                sessionEventSink,
              )
            is FlowTimeSession.State.Complete ->
              SessionComplete(
                duration = flowTimeState.sessionDuration.inWholeSeconds.toString(),
                breakRecommendation = flowTimeState.recommendedBreak,
                sessionEventSink,
              )
          }
        }
      }
    }

    return State(
      content = content,
      eventSink = eventSink
    )
  }

}
