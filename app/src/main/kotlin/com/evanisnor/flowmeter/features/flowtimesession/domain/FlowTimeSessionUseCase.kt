package com.evanisnor.flowmeter.features.flowtimesession.domain

import com.evanisnor.flowmeter.FeatureFlags
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.di.SingleIn
import com.evanisnor.flowmeter.features.flowtimesession.domain.FlowTimeSessionUseCase.FlowState
import com.evanisnor.flowmeter.features.settings.data.SettingsRepository
import com.evanisnor.flowmeter.system.NotificationSystem
import com.evanisnor.flowmeter.system.WorkManagerSystem
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

  suspend fun beginTakeABreak()

  fun stop()
}

object NoOpFlowTimeSessionUseCase : FlowTimeSessionUseCase {
  override suspend fun beginFlowSession() = Unit

  override suspend fun beginTakeABreak() = Unit

  override fun stop() = Unit

  override suspend fun collect(collector: FlowCollector<FlowState>) = Unit
}

private val DEBUG_QUICK_BREAK_RECOMMENDATION = 5.seconds

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, FlowTimeSessionUseCase::class)
class RealFlowTimeSessionUseCase
  @Inject
  constructor(
    private val flowTimeSessionProvider: Provider<FlowTimeSession>,
    private val attentionGrabber: AttentionGrabber,
    private val notificationSystem: NotificationSystem,
    private val workManagerSystem: WorkManagerSystem,
    private val settingsRepository: SettingsRepository,
    private val timeFormatter: TimeFormatter,
  ) : FlowTimeSessionUseCase {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val state: MutableStateFlow<FlowState> = MutableStateFlow(FlowState.Idle)

    private val isTakingABreak = AtomicBoolean(false)
    private val notifiedBreakIsOver = AtomicBoolean(false)
    private val breakRecommendation = AtomicReference(0.minutes)
    private val currentSession = AtomicReference<FlowTimeSession>(NoOpFlowTimeSession)

    override suspend fun beginFlowSession() {
      Timber.i("Starting a new Flow session")
      checkForNotificationPermission()
      currentSession.get().stop()
      isTakingABreak.set(false)
      if (FeatureFlags.FLOW_IN_WORKMANAGER) {
        workManagerSystem.startFlowTime()
      } else {
        flowTimeSessionProvider.get()
      }.let {
        currentSession.set(it)
        collectFromSession(it)
      }
      attentionGrabber.notifySessionStarted()
    }

    override suspend fun beginTakeABreak() {
      Timber.i("Taking a break")
      isTakingABreak.set(true)
      if (FeatureFlags.FLOW_IN_WORKMANAGER) {
        workManagerSystem.startFlowTime()
      } else {
        flowTimeSessionProvider.get()
      }.let {
        currentSession.set(it)
        collectFromSession(it)
      }
    }

    override fun stop() {
      if (isTakingABreak.get()) {
        Timber.i("Break is over")
      } else {
        Timber.i("Flow session is complete")
      }
      currentSession.get().stop()
    }

    override suspend fun collect(collector: FlowCollector<FlowState>) = collector.emitAll(state)

    private fun collectFromSession(session: FlowTimeSession) {
      scope.launch {
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
    }

    private suspend fun processForSideEffects(state: FlowState) {
      when (state) {
        is FlowState.Idle -> {
          // No side-effects
        }
        is FlowState.InTheFlow -> {
          // No side-effects
        }
        is FlowState.FlowComplete -> {
          breakRecommendation.set(
            if (settingsRepository.getDebugQuickBreaks()) DEBUG_QUICK_BREAK_RECOMMENDATION else state.recommendedBreak,
          )
        }
        is FlowState.TakingABreak -> {
          if (state.isBreakLongerThanRecommended && !notifiedBreakIsOver.get()) {
            attentionGrabber.notifyBreakIsOver()
            notifiedBreakIsOver.set(true)
          }
        }
        FlowState.BreakIsOver -> {
          notifiedBreakIsOver.set(false)
        }
      }
    }

    private fun checkForNotificationPermission() {
      if (!notificationSystem.isNotificationPermissionGranted()) {
        notificationSystem.requestPermission()
      }
    }

    private fun FlowTimeSession.State.toFlowState() =
      when (this) {
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

    private fun FlowTimeSession.State.toBreakFlowState() =
      when (this) {
        is FlowTimeSession.State.Tick ->
          FlowState.TakingABreak(
            duration = timeFormatter.humanReadableClock(duration),
            breakRecommendation = breakRecommendation.get(),
            isBreakLongerThanRecommended = duration >= breakRecommendation.get(),
          )
        is FlowTimeSession.State.Complete -> FlowState.BreakIsOver
      }
  }
