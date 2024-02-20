package com.evanisnor.flowmeter.features.flowtimesession.domain

import android.media.RingtoneManager
import com.evanisnor.flowmeter.system.NotificationPublisher
import javax.inject.Inject

private const val BREAK_IS_OVER_NOTIFICATION = 234234

/**
 * Grabs the user's attention by shouting and shaking.
 */
class AttentionGrabber @Inject constructor(
  private val ringtoneManager: RingtoneManager,
  private val notificationPublisher: NotificationPublisher,
) {

  fun notifySessionStarted() {
    ringtoneManager.getRingtone(RingtoneManager.TYPE_NOTIFICATION).play()
  }

  fun notifyBreakIsOver() {
    notificationPublisher.post(
      NotificationPublisher.Notification(
        id = BREAK_IS_OVER_NOTIFICATION,
        title = "Break time is over!",
      )
    )
    ringtoneManager.getRingtone(RingtoneManager.TYPE_NOTIFICATION).play()
  }

  fun clearBreakIsOverNotification() {
    notificationPublisher.cancel(BREAK_IS_OVER_NOTIFICATION)
  }

}
