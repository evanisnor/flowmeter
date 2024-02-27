package com.evanisnor.flowmeter.system

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.evanisnor.flowmeter.MainActivity
import javax.inject.Inject

/**
 * Provides pre-built Intents and PendingIntents for use elsewhere.
 */
class IntentProvider
  @Inject
  constructor(
    context: Context,
  ) {
    private val openMainActivityIntent =
      Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
      }

    val openApp: PendingIntent =
      PendingIntent.getActivity(context, 0, openMainActivityIntent, PendingIntent.FLAG_IMMUTABLE)
  }
