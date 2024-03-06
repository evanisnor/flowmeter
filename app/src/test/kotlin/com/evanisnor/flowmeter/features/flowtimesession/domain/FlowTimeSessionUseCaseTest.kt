@file:OptIn(ExperimentalCoroutinesApi::class)

package com.evanisnor.flowmeter.features.flowtimesession.domain

import app.cash.turbine.test
import com.evanisnor.flowmeter.features.flowtimesession.domain.FlowTimeSessionUseCase.FlowState
import com.evanisnor.flowmeter.features.settings.data.FakeSettingsRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class FlowTimeSessionUseCaseTest {

  private val flowTimeSession = FakeFlowTimeSession()
  private val underTest = RealFlowTimeSessionUseCase(
    flowTimeSessionProvider = { flowTimeSession },
    attentionGrabber = FakeAttentionGrabber(),
    settingsRepository = FakeSettingsRepository(),
    timeFormatter = FakeTimeFormatter(),
    scope = CoroutineScope(Dispatchers.Unconfined),
  )

  @Test
  fun `beginFlowSession - when collected - emits Idle`() = runTest {
    underTest.beginFlowSession()

    underTest.test {
      assertThat(awaitItem()).isEqualTo(FlowState.Idle)
    }
  }

  @Test
  fun `beginFlowSession - when collected - emits InTheFlow`() = runTest {
    underTest.beginFlowSession()

    underTest.test {
      awaitItem() // Idle
      flowTimeSession.collector!!.emit(FlowTimeSession.State.Tick(Duration.ZERO))

      assertThat(awaitItem()).isEqualTo(FlowState.InTheFlow(Duration.ZERO.toIsoString()))
    }
  }

  @Test
  fun `beginFlowSession - when stopped - emits FlowComplete`() = runTest {
    underTest.beginFlowSession()

    underTest.test {
      awaitItem() // Idle
      underTest.stop()
      assertThat(
        awaitItem(),
      ).isEqualTo(FlowState.FlowComplete(Duration.ZERO.toIsoString(), Duration.ZERO))
    }
  }

  @Test
  fun `beginTakeABreak - when collected - emits Idle`() = runTest {
    underTest.beginTakeABreak(5.seconds)

    underTest.test {
      assertThat(awaitItem()).isEqualTo(FlowState.Idle)
    }
  }

  @Test
  fun `beginTakeABreak - when collected - emits TakingABreak`() = runTest {
    underTest.beginTakeABreak(5.seconds)

    underTest.test {
      awaitItem() // Idle
      flowTimeSession.collector!!.emit(FlowTimeSession.State.Tick(Duration.ZERO))

      assertThat(awaitItem()).isEqualTo(
        FlowState.TakingABreak(
          duration = Duration.ZERO.toIsoString(),
          breakRecommendation = 5.seconds,
          isBreakLongerThanRecommended = false,
        ),
      )
    }
  }

  @Test
  fun `beginTakeABreak - when stopped - emits BreakIsOver`() = runTest {
    underTest.beginTakeABreak(5.seconds)

    underTest.test {
      awaitItem() // Idle
      underTest.stop()
      assertThat(awaitItem()).isEqualTo(FlowState.BreakIsOver)
    }
  }
}
