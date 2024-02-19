package com.evanisnor.flowmeter.features.flowtimesession.domain

import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
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
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      VibrationEffect.startComposition()
        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_SLOW_RISE)
        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK)
        .compose()
    } else {
      VibrationEffect.createOneShot(200L, VibrationEffect.DEFAULT_AMPLITUDE)
    }
    ringtoneManager.getRingtone(RingtoneManager.TYPE_NOTIFICATION).play()
  }

}
