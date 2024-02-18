package com.evanisnor.flowmeter.features.flowsession

import com.evanisnor.flowmeter.features.flowsession.FlowSession.State
import com.evanisnor.flowmeter.features.flowsession.FlowSession.State.Complete
import com.evanisnor.flowmeter.features.flowsession.FlowSession.State.Tick
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


/**
 * Flow tracking session!
 */
interface FlowSession : Flow<State> {

  sealed interface State {
    data class Tick(val duration: Duration) : State
    data class Complete(
      val totalDuration: Duration,
      val recommendedBreak: Duration,
    ) : State
  }

  fun stop()

}

/**
 * Doing a [FlowSession]
 */
class FlowSessionLogic(
  private val timeProvider: TimeProvider = RealTimeProvider(),
) : FlowSession {

  private val isRunning: AtomicBoolean = AtomicBoolean(true)

  override suspend fun collect(collector: FlowCollector<State>) {
     val start = timeProvider.now().epochSecond.seconds
     var secondsSinceStart = 0.seconds

     while (isRunning.get()) {
       secondsSinceStart = timeProvider.now().epochSecond.seconds - start
       collector.emit(Tick(secondsSinceStart))
       delay(1.seconds)
     }

     Complete(
       totalDuration = secondsSinceStart,
       recommendedBreak = 0.seconds,
     ).run { collector.emit(this) }
  }

  override fun stop() {
    isRunning.set(false)
  }

}
