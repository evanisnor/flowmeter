package com.evanisnor.flowmeter.features.flowtimesession.domain

import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.features.flowtimesession.domain.FlowTimeSessionUseCase.FlowState
import com.evanisnor.flowmeter.features.settings.data.SettingsRepository
import com.evanisnor.flowmeter.system.MainScope
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Provider
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

interface FlowTimeSessionUseCase : Flow<FlowState> {
  sealed interface FlowState {
    data object Idle : FlowState

    data class InTheFlow(val duration: String) : FlowState

    data class FlowComplete(
      val duration: String,
      val recommendedBreak: Duration,
    ) : FlowState

    data class TakingABreak(
      val duration: String,
      val breakRecommendation: Duration,
      val isBreakLongerThanRecommended: Boolean,
    ) : FlowState

    data object BreakIsOver : FlowState
  }

  suspend fun beginFlowSession()

  suspend fun beginTakeABreak(breakRecommendation: Duration)

  suspend fun stop()
}

object NoOpFlowTimeSessionUseCase : FlowTimeSessionUseCase {
  override suspend fun beginFlowSession() = Unit

  override suspend fun beginTakeABreak(breakRecommendation: Duration) = Unit

  override suspend fun stop() = Unit

  override suspend fun collect(collector: FlowCollector<FlowState>) = Unit
}

private val DEBUG_QUICK_BREAK_RECOMMENDATION = 5.seconds

@ContributesBinding(AppScope::class, FlowTimeSessionUseCase::class)
class RealFlowTimeSessionUseCase
@Inject
constructor(
  private val flowTimeSessionProvider: Provider<FlowTimeSession>,
  private val attentionGrabber: AttentionGrabber,
  private val settingsRepository: SettingsRepository,
  private val timeFormatter: TimeFormatter,
  @MainScope private val scope: CoroutineScope,
) : FlowTimeSessionUseCase {
  private val state: MutableStateFlow<FlowState> = MutableStateFlow(FlowState.Idle)

  private val isTakingABreak = AtomicBoolean(false)
  private val notifiedBreakIsOver = AtomicBoolean(false)
  private val breakRecommendation = AtomicReference(0.minutes)
  private val currentSession = AtomicReference<FlowTimeSession>(NoOpFlowTimeSession)
  private val currentCollectJob = AtomicReference<Job?>(null)

  override suspend fun beginFlowSession() {
    Timber.i("Starting a new Flow session")
    flowTimeSessionProvider.get().let { session ->
      currentSession.set(session)
      collectFromSession(session).let { currentCollectJob.set(it) }
    }
    attentionGrabber.notifySessionStarted()
  }

  override suspend fun beginTakeABreak(breakRecommendation: Duration) {
    Timber.i("Taking a break")
    if (settingsRepository.getDebugQuickBreaks()) {
      this.breakRecommendation.set(DEBUG_QUICK_BREAK_RECOMMENDATION)
    } else {
      this.breakRecommendation.set(breakRecommendation)
    }
    isTakingABreak.set(true)
    flowTimeSessionProvider.get().let { session ->
      currentSession.set(session)
      collectFromSession(session).let { currentCollectJob.set(it) }
    }
  }

  override suspend fun stop() {
    if (isTakingABreak.get()) {
      attentionGrabber.clearBreakIsOverNotification()
      currentSession.get().stop()
      Timber.i("Break is over")
    } else {
      currentSession.get().stop()
      Timber.i("Flow session is complete")
    }
    currentCollectJob.get()?.cancel()
  }

  override suspend fun collect(collector: FlowCollector<FlowState>) = collector.emitAll(state)

  private fun collectFromSession(session: FlowTimeSession) = scope.launch {
    session.collect { sessionState ->
      if (isTakingABreak.get()) {
        sessionState.toBreakFlowState()
      } else {
        sessionState.toFlowState()
      }
        .also { processForSideEffects(it) }
        .run { state.emit(this) }
    }
  }

  private suspend fun processForSideEffects(state: FlowState) {
    when (state) {
      is FlowState.TakingABreak -> {
        if (state.isBreakLongerThanRecommended && !notifiedBreakIsOver.get()) {
          attentionGrabber.notifyBreakIsOver()
          notifiedBreakIsOver.set(true)
        }
      }
      is FlowState.BreakIsOver -> {
        isTakingABreak.set(false)
        notifiedBreakIsOver.set(false)
      }
      else -> { /* No other side-effects */ }
    }
  }

  private fun FlowTimeSession.State.toFlowState() = when (this) {
    is FlowTimeSession.State.Tick ->
      FlowState.InTheFlow(
        duration = timeFormatter.humanReadableClock(duration),
      )
    is FlowTimeSession.State.Complete ->
      FlowState.FlowComplete(
        duration = timeFormatter.humanReadableSentence(sessionDuration),
        recommendedBreak = recommendedBreak,
      )
  }

  private fun FlowTimeSession.State.toBreakFlowState() = when (this) {
    is FlowTimeSession.State.Tick ->
      FlowState.TakingABreak(
        duration = timeFormatter.humanReadableClock(duration),
        breakRecommendation = breakRecommendation.get(),
        isBreakLongerThanRecommended = duration >= breakRecommendation.get(),
      )
    is FlowTimeSession.State.Complete -> FlowState.BreakIsOver
  }
}
