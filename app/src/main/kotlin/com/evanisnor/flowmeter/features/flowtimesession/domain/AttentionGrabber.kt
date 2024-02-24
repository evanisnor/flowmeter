package com.evanisnor.flowmeter.features.flowtimesession.domain

import android.media.RingtoneManager
import com.evanisnor.flowmeter.features.settings.data.SettingsRepository
import com.evanisnor.flowmeter.system.MediaPlayerSystem
import com.evanisnor.flowmeter.system.NotificationPublisher
import com.evanisnor.flowmeter.system.RingtoneSystem
import javax.inject.Inject

private const val BREAK_IS_OVER_NOTIFICATION = 234234

/**
 * Grabs the user's attention by shouting and shaking.
 */
class AttentionGrabber @Inject constructor(
  private val settingsRepository: SettingsRepository,
  private val mediaPlayerSystem: MediaPlayerSystem,
  private val notificationPublisher: NotificationPublisher,
) {

  suspend fun notifySessionStarted() {
    mediaPlayerSystem.play(settingsRepository.getSessionStartSound())
  }

  suspend fun notifyBreakIsOver() {
    mediaPlayerSystem.play(settingsRepository.getBreakIsOverSound())
    notificationPublisher.post(
      NotificationPublisher.Notification(
        id = BREAK_IS_OVER_NOTIFICATION,
        title = "Break time is over!",
      )
    )
  }

  fun clearBreakIsOverNotification() {
    notificationPublisher.cancel(BREAK_IS_OVER_NOTIFICATION)
  }

}
