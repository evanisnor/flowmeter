package com.evanisnor.flowmeter.features.flowsession

import com.evanisnor.flowmeter.features.flowsession.FlowTimeSession.State
import com.evanisnor.flowmeter.features.flowsession.FlowTimeSession.State.Complete
import com.evanisnor.flowmeter.features.flowsession.FlowTimeSession.State.Tick
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import java.util.concurrent.atomic.AtomicBoolean
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
 * Doing a [FlowTimeSession]. Based on "Flow Time"
 * https://www.insightful.io/blog/flowtime-pomodoro-alternative
 */
class FlowTimeSessionLogic(
  private val timeProvider: TimeProvider = RealTimeProvider(),
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
