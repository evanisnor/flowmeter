package com.evanisnor.flowmeter.features.flowsession.domain

import androidx.annotation.VisibleForTesting
import java.time.Instant
import kotlin.time.Duration.Companion.minutes

interface TimeProvider {
  fun now() : Instant
}

class RealTimeProvider : TimeProvider {
  override fun now(): Instant = Instant.now()
}

@VisibleForTesting(otherwise = VisibleForTesting.NONE)
class FakeTimeProvider(
  private var now: Instant = Instant.ofEpochSecond(0L),
) : TimeProvider {

  override fun now(): Instant = now

  fun setSeconds(seconds: Int) {
    now = Instant.ofEpochSecond(seconds.toLong())
  }

  fun setMinutes(minutes: Int) {
    now = Instant.ofEpochSecond(minutes.minutes.inWholeSeconds)
  }
}
