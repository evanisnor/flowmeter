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
import com.evanisnor.flowmeter.features.flowtimesession.domain.FlowTimeSessionUseCase
import com.evanisnor.flowmeter.features.flowtimesession.domain.NoOpFlowTimeSessionUseCase
import com.evanisnor.flowmeter.features.flowtimesession.domain.startFlowTimeSession
import com.evanisnor.flowmeter.system.MainScope
import com.evanisnor.flowmeter.system.WorkManagerSystem
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class SessionContentPresenter
@Inject
constructor(
  private val workManagerSystem: WorkManagerSystem,
  @MainScope private val scope: CoroutineScope,
) : Presenter<SessionContent> {
  @OptIn(ExperimentalCoroutinesApi::class)
  @Composable
  override fun present(): SessionContent {
    val session =
      rememberRetained {
        mutableStateOf<FlowTimeSessionUseCase>(NoOpFlowTimeSessionUseCase)
      }
    val breakRecommendation = rememberRetained { mutableStateOf(0.seconds) }

    val eventSink: (SessionEvent) -> Unit = { event ->
      scope.launch {
        when (event) {
          is NewSession -> {
            session.value = newSession()
            session.value.beginFlowSession()
          }
          is EndSession -> {
            session.value.stop()
          }
          is TakeABreak -> {
            session.value = newSession()
            session.value.beginTakeABreak(breakRecommendation.value)
          }
          is EndBreak -> {
            session.value.stop()
          }
        }
      }
    }

    val sessionContent by produceRetainedState<SessionContent>(
      initialValue = StartNew(eventSink),
      key1 = session.value,
    ) {
      snapshotFlow { session.value }.flattenConcat().collect { flowState ->
        value =
          when (flowState) {
            is FlowTimeSessionUseCase.FlowState.InTheFlow ->
              SessionInProgress(
                duration = flowState.duration,
                eventSink = eventSink,
              )
            is FlowTimeSessionUseCase.FlowState.FlowComplete ->
              SessionComplete(
                duration = flowState.duration,
                breakRecommendation = flowState.recommendedBreak,
                eventSink = eventSink,
              ).also {
                breakRecommendation.value = flowState.recommendedBreak
              }
            is FlowTimeSessionUseCase.FlowState.TakingABreak ->
              SessionContent.TakingABreak(
                duration = flowState.duration,
                breakRecommendation = flowState.breakRecommendation,
                isBreakLongerThanRecommended = flowState.isBreakLongerThanRecommended,
                eventSink = eventSink,
              )
            is FlowTimeSessionUseCase.FlowState.Idle,
            is FlowTimeSessionUseCase.FlowState.BreakIsOver,
            -> StartNew(eventSink)
          }
      }
    }

    return sessionContent
  }

  private suspend fun newSession(): FlowTimeSessionUseCase =
    workManagerSystem.startFlowTimeSession()
}
