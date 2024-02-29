package com.evanisnor.flowmeter.features.flowtimesession.domain

import javax.inject.Inject
import kotlin.time.Duration

class TimeFormatter
@Inject
constructor() {
  fun humanReadableClock(duration: Duration): String {
    return if (duration.inWholeHours > 0) {
      "${duration.inWholeHours}:${format(
        duration.inWholeMinutes % 60,
      )}:${format(duration.inWholeSeconds % 60)}"
    } else {
      "${duration.inWholeMinutes}:${format(duration.inWholeSeconds % 60)}"
    }
  }

  fun humanReadableSentence(duration: Duration): String {
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
