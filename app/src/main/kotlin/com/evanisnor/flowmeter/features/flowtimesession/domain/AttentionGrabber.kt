package com.evanisnor.flowmeter.features.flowtimesession.domain

import androidx.core.app.NotificationCompat
import com.evanisnor.flowmeter.features.settings.data.SettingsRepository
import com.evanisnor.flowmeter.system.MediaPlayerSystem
import com.evanisnor.flowmeter.system.NotificationChannelSystem.NotificationChannel.BreakIsOverNotificationChannel
import com.evanisnor.flowmeter.system.NotificationPublisher
import javax.inject.Inject

private const val BREAK_IS_OVER_NOTIFICATION = 234234

/**
 * Grabs the user's attention by shouting and shaking.
 */
class AttentionGrabber
@Inject
constructor(
  private val settingsRepository: SettingsRepository,
  private val mediaPlayerSystem: MediaPlayerSystem,
  private val notificationPublisher: NotificationPublisher,
) {
  suspend fun notifySessionStarted() {
    mediaPlayerSystem.play(settingsRepository.getSessionStartSound())
  }

  suspend fun notifyBreakIsOver() {
    notificationPublisher.post(
      notification =
      NotificationPublisher.Notification(
        id = BREAK_IS_OVER_NOTIFICATION,
        title = "Break time is over!",
        priority = NotificationCompat.PRIORITY_HIGH,
        sound = settingsRepository.getBreakIsOverSound(),
      ),
      channel = BreakIsOverNotificationChannel,
    )
  }

  fun clearBreakIsOverNotification() {
    notificationPublisher.cancel(BREAK_IS_OVER_NOTIFICATION)
  }
}
