package com.evanisnor.flowmeter.features.flowtimesession.domain

import androidx.annotation.VisibleForTesting
import com.evanisnor.flowmeter.di.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import kotlin.time.Duration

interface TimeFormatter {
  fun humanReadableClock(duration: Duration): String
  fun humanReadableSentence(duration: Duration): String
}

@ContributesBinding(AppScope::class, TimeFormatter::class)
class RealTimeFormatter @Inject constructor() : TimeFormatter {
  override fun humanReadableClock(duration: Duration): String {
    return if (duration.inWholeHours > 0) {
      "${duration.inWholeHours}:${format(
        duration.inWholeMinutes % 60,
      )}:${format(duration.inWholeSeconds % 60)}"
    } else {
      "${duration.inWholeMinutes}:${format(duration.inWholeSeconds % 60)}"
    }
  }

  override fun humanReadableSentence(duration: Duration): String {
    return if (duration.inWholeHours >= 1L) {
      "${humanReadableHours(
        duration.inWholeHours,
      )} and ${humanReadableMinutes(duration.inWholeMinutes % 60)}"
    } else if (duration.inWholeMinutes >= 1L) {
      humanReadableMinutes(duration.inWholeMinutes)
    } else {
      humanReadableSeconds(duration.inWholeSeconds)
    }
  }

  private fun humanReadableHours(hours: Long): String {
    return if (hours == 1L) {
      "$hours hour"
    } else {
      "$hours hours"
    }
  }

  private fun humanReadableMinutes(minutes: Long): String {
    return if (minutes == 1L) {
      "$minutes minute"
    } else {
      "$minutes minutes"
    }
  }

  private fun humanReadableSeconds(seconds: Long): String {
    return if (seconds == 1L) {
      "$seconds second"
    } else {
      "$seconds seconds"
    }
  }

  private fun format(n: Long) = n.toString().padStart(2, '0')
}

@VisibleForTesting(otherwise = VisibleForTesting.NONE)
class FakeTimeFormatter : TimeFormatter {
  override fun humanReadableClock(duration: Duration): String = duration.toIsoString()
  override fun humanReadableSentence(duration: Duration): String = duration.toIsoString()
}
