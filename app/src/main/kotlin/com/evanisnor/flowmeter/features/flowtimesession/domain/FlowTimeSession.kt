package com.evanisnor.flowmeter.features.flowtimesession.domain

import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.features.flowtimesession.domain.FlowTimeSession.State
import com.evanisnor.flowmeter.features.flowtimesession.domain.FlowTimeSession.State.Complete
import com.evanisnor.flowmeter.features.flowtimesession.domain.FlowTimeSession.State.Tick
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


/**
 * Flow tracking session!
 */
interface FlowTimeSession : Flow<State> {

  sealed interface State {
    data class Tick(val duration: Duration) : State
    data class Complete(
      val sessionDuration: Duration,
      val recommendedBreak: Duration,
    ) : State
  }

  fun stop()

}

/**
 * [FlowTimeSession] that does not do anything. Used as an initial state.
 */
object NoOpFlowTimeSession : FlowTimeSession {
  override suspend fun collect(collector: FlowCollector<State>) = Unit
  override fun stop() = Unit
}

/**
 * Doing a [FlowTimeSession]. Based on "Flow Time"
 * https://www.insightful.io/blog/flowtime-pomodoro-alternative
 */
@ContributesBinding(AppScope::class, FlowTimeSession::class)
class FlowTimeSessionLogic @Inject constructor(
  private val timeProvider: TimeProvider,
) : FlowTimeSession {

  private val isRunning: AtomicBoolean = AtomicBoolean(true)

  override suspend fun collect(collector: FlowCollector<State>) {
    val start = timeProvider.now().epochSecond.seconds
    var sessionDuration = 0.seconds

    while (isRunning.get()) {
      sessionDuration = timeProvider.now().epochSecond.seconds - start
      collector.emit(Tick(sessionDuration))
      delay(1.seconds)
    }

    Complete(
      sessionDuration = sessionDuration,
      recommendedBreak = sessionDuration.recommendedBreak(),
    ).run { collector.emit(this) }
  }

  override fun stop() {
    isRunning.set(false)
  }

  private fun Duration.recommendedBreak(): Duration = when {
    this < 25.minutes -> 5.minutes
    this < 50.minutes -> 8.minutes
    this < 90.minutes -> 10.minutes
    this < 120.minutes -> 15.minutes
    else -> 20.minutes
  }

}