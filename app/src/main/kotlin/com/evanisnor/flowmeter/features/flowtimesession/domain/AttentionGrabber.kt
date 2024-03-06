package com.evanisnor.flowmeter.features.flowtimesession.domain

import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationCompat
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.features.settings.data.SettingsRepository
import com.evanisnor.flowmeter.system.MediaPlayerSystem
import com.evanisnor.flowmeter.system.NotificationChannelSystem.NotificationChannel.BreakIsOverNotificationChannel
import com.evanisnor.flowmeter.system.NotificationPublisher
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

private const val BREAK_IS_OVER_NOTIFICATION = 234234

interface AttentionGrabber {
  suspend fun notifySessionStarted()

  suspend fun notifyBreakIsOver()

  fun clearBreakIsOverNotification()
}

/**
 * Grabs the user's attention by shouting and shaking.
 */
@ContributesBinding(AppScope::class, AttentionGrabber::class)
class RealAttentionGrabber
@Inject
constructor(
  private val settingsRepository: SettingsRepository,
  private val mediaPlayerSystem: MediaPlayerSystem,
  private val notificationPublisher: NotificationPublisher,
) : AttentionGrabber {

  override suspend fun notifySessionStarted() {
    mediaPlayerSystem.play(settingsRepository.getSessionStartSound())
  }

  override suspend fun notifyBreakIsOver() {
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

  override fun clearBreakIsOverNotification() {
    notificationPublisher.cancel(BREAK_IS_OVER_NOTIFICATION)
  }
}

@VisibleForTesting(otherwise = VisibleForTesting.NONE)
class FakeAttentionGrabber : AttentionGrabber {
  override suspend fun notifySessionStarted() = Unit
  override suspend fun notifyBreakIsOver() = Unit
  override fun clearBreakIsOverNotification() = Unit
}
