package com.evanisnor.flowmeter.features.flowtimesession.domain

import androidx.annotation.VisibleForTesting
import com.evanisnor.flowmeter.di.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

interface TimeProvider {
  fun now(): Instant
}

@ContributesBinding(AppScope::class, TimeProvider::class)
class RealTimeProvider
  @Inject
  constructor() : TimeProvider {
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
