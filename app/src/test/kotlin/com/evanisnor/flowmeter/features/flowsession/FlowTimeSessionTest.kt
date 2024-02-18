package com.evanisnor.flowmeter.features.flowsession

import app.cash.turbine.test
import com.evanisnor.flowmeter.features.flowsession.FlowTimeSession.State.Complete
import com.evanisnor.flowmeter.features.flowsession.FlowTimeSession.State.Tick
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Test for [FlowTimeSession]
 */
class FlowTimeSessionTest {

  private val timeProvider = FakeTimeProvider()
  private val flowSession = FlowTimeSessionLogic(
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
          sessionDuration = 0.seconds,
          recommendedBreak = 5.minutes,
        )
      )
      awaitComplete()
    }
  }

  @Test
  fun `session - when collecting before stopping - emits Complete`() = runTest {
    timeProvider.setSeconds(100)

    flowSession.test {
      awaitItem()
      timeProvider.setSeconds(101)
      awaitItem()

      flowSession.stop()
      assertThat(awaitItem()).isEqualTo(
        Complete(
          sessionDuration = 1.seconds,
          recommendedBreak = 5.minutes,
        )
      )
      awaitComplete()
    }
  }

  @Test
  fun `session - when duration is 25 minutes - recommends an 8 minute break`() = runTest {
    flowSession.test {
      awaitItem()
      timeProvider.setMinutes(25)
      awaitItem()

      flowSession.stop()
      assertThat(awaitItem()).isEqualTo(
        Complete(
          sessionDuration = 25.minutes,
          recommendedBreak = 8.minutes,
        )
      )
      awaitComplete()
    }
  }

  @Test
  fun `session - when duration is 50 minutes - recommends a 10 minute break`() = runTest {
    flowSession.test {
      awaitItem()
      timeProvider.setMinutes(50)
      awaitItem()

      flowSession.stop()
      assertThat(awaitItem()).isEqualTo(
        Complete(
          sessionDuration = 50.minutes,
          recommendedBreak = 10.minutes,
        )
      )
      awaitComplete()
    }
  }

  @Test
  fun `session - when duration is 90 minutes - recommends a 15 minute break`() = runTest {
    flowSession.test {
      awaitItem()
      timeProvider.setMinutes(90)
      awaitItem()

      flowSession.stop()
      assertThat(awaitItem()).isEqualTo(
        Complete(
          sessionDuration = 90.minutes,
          recommendedBreak = 15.minutes,
        )
      )
      awaitComplete()
    }
  }

  @Test
  fun `session - when duration is 120 minutes - recommends a 20 minute break`() = runTest {
    flowSession.test {
      awaitItem()
      timeProvider.setMinutes(120)
      awaitItem()

      flowSession.stop()
      assertThat(awaitItem()).isEqualTo(
        Complete(
          sessionDuration = 120.minutes,
          recommendedBreak = 20.minutes,
        )
      )
      awaitComplete()
    }
  }

}
