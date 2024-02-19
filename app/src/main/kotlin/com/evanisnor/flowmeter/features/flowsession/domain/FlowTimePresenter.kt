package com.evanisnor.flowmeter.features.flowsession.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.Event
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.Event.EndSession
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.Event.NewSession
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.State
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.State.SessionComplete
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.State.SessionInProgress
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.State.StartNew
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@CircuitInject(FlowTimeScreen::class, AppScope::class)
class FlowTimePresenter @Inject constructor(
  private val scope: CoroutineScope,
  private val flowTimeSessionProvider: Provider<FlowTimeSession>,
) : Presenter<State> {

  @Composable
  override fun present(): State {
    val latestEvent = rememberRetained { mutableStateOf<Event?>(null) }
    val session = rememberRetained { mutableStateOf<FlowTimeSession?>(null) }

    val eventSink: (Event) -> Unit = { event -> latestEvent.value = event }

    val state by produceRetainedState<State>(initialValue = StartNew(eventSink)) {
      snapshotFlow { latestEvent.value }.collectLatest {
        when (it) {
          is NewSession -> {
            session.value = flowTimeSessionProvider.get()
            scope.launch {

              session.value?.collectLatest { flowTimeState ->
                value = when (flowTimeState) {
                  is FlowTimeSession.State.Tick -> {
                    SessionInProgress(
                      duration = flowTimeState.duration.inWholeSeconds.toString(),
                      eventSink = eventSink
                    )
                  }
                  is FlowTimeSession.State.Complete -> {
                    SessionComplete(
                      duration = flowTimeState.sessionDuration.inWholeSeconds.toString(),
                      breakRecommendation = flowTimeState.recommendedBreak,
                      eventSink = eventSink,
                    ).also {
                      cancel()
                    }
                  }
                }
              }
            }
          }
          is EndSession -> {
            session.value?.stop()
          }
        }
      }
    }
    return state
  }

}
