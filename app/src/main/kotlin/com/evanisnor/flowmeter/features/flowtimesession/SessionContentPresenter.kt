package com.evanisnor.flowmeter.features.flowtimesession

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionComplete
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent.EndBreak
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent.EndSession
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent.NewSession
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent.TakeABreak
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionInProgress
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.StartNew
import com.evanisnor.flowmeter.features.flowtimesession.domain.FlowTimeSessionUseCase
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.launch
import javax.inject.Inject

class SessionContentPresenter @Inject constructor(
  private val flowTimeSessionUseCase: FlowTimeSessionUseCase,
) : Presenter<SessionContent> {

  @Composable
  override fun present(): SessionContent {
    val scope = rememberCoroutineScope()

    val eventSink: (SessionEvent) -> Unit = { event ->
      when (event) {
        is NewSession -> scope.launch {
            flowTimeSessionUseCase.beginFlowSession()
          }
        is EndSession -> flowTimeSessionUseCase.stop()
        is TakeABreak -> scope.launch {
            flowTimeSessionUseCase.beginTakeABreak()
          }
        is EndBreak -> flowTimeSessionUseCase.stop()
      }
    }

    val sessionContent by produceRetainedState<SessionContent>(initialValue = StartNew(eventSink)) {
      flowTimeSessionUseCase.collect { flowState ->
        value = when (flowState) {
          is FlowTimeSessionUseCase.FlowState.InTheFlow -> SessionInProgress(
            duration = flowState.duration,
            eventSink = eventSink
          )
          is FlowTimeSessionUseCase.FlowState.FlowComplete -> SessionComplete(
            duration = flowState.duration,
            breakRecommendation = flowState.recommendedBreak,
            eventSink = eventSink,
          )
          is FlowTimeSessionUseCase.FlowState.TakingABreak -> SessionContent.TakingABreak(
            duration = flowState.duration,
            breakRecommendation = flowState.breakRecommendation,
            isBreakLongerThanRecommended = flowState.isBreakLongerThanRecommended,
            eventSink = eventSink,
          )
          FlowTimeSessionUseCase.FlowState.Idle,
          FlowTimeSessionUseCase.FlowState.BreakIsOver -> StartNew(eventSink)
        }
      }
    }

    return sessionContent
  }
}
