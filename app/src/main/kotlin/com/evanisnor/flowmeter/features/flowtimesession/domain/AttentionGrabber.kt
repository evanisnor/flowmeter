package com.evanisnor.flowmeter.features.flowtimesession.domain

import android.media.RingtoneManager
import javax.inject.Inject

/**
 * Grabs the user's attention by shouting and shaking.
 */
class AttentionGrabber @Inject constructor(
  private val ringtoneManager: RingtoneManager,
) {

  fun playSessionStartNoise() {
    ringtoneManager.getRingtone(RingtoneManager.TYPE_NOTIFICATION).play()
  }

  fun playBreakEndNoise() {
    ringtoneManager.getRingtone(RingtoneManager.TYPE_NOTIFICATION).play()
  }

}
