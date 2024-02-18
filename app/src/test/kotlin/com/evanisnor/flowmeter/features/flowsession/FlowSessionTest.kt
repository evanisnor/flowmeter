package com.evanisnor.flowmeter.features.flowsession

import app.cash.turbine.test
import com.evanisnor.flowmeter.features.flowsession.FlowSession.State.Complete
import com.evanisnor.flowmeter.features.flowsession.FlowSession.State.Tick
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

/**
 * Test for [FlowSession]
 */
class FlowSessionTest {

  private val timeProvider = FakeTimeProvider()
  private val flowSession = FlowSessionLogic(
    timeProvider = timeProvider
  )

  @Test
  fun `session - when collecting - emits Ticks over time`() = runTest {
    timeProvider.setSeconds(100)

    flowSession.test {
      assertThat(awaitItem()).isEqualTo(Tick(0.seconds))
      timeProvider.setSeconds(101)
      assertThat(awaitItem()).isEqualTo(Tick(1.seconds))
      timeProvider.setSeconds(102)
      assertThat(awaitItem()).isEqualTo(Tick(2.seconds))

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `session - when stopped - emits Complete`() = runTest {
    timeProvider.setSeconds(100)
    flowSession.stop()

    flowSession.test {
      assertThat(awaitItem()).isEqualTo(
        Complete(
          totalDuration = 0.seconds,
          recommendedBreak = 0.seconds,
        )
      )
      awaitComplete()
    }
  }

}
