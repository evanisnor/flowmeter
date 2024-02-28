package com.evanisnor.flowmeter.system

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.PermissionChecker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import com.evanisnor.flowmeter.R
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.di.SingleIn
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

private const val POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"

/**
 * Manage system notification integration
 */
interface NotificationSystem {
  fun isNotificationPermissionGranted(): Boolean

  fun registerForPermissionResult(activity: ComponentActivity)

  fun requestPermission()
}

/**
 * Send notifications to the user
 */
interface NotificationPublisher {
  data class Notification(
    val id: Int,
    val title: String,
    val priority: Int,
    val sound: RingtoneSystem.RingtoneSound? = null,
    val ongoing: Boolean = false,
  )

  suspend fun post(
    notification: Notification,
    channel: NotificationChannelSystem.NotificationChannel,
  )

  suspend fun post(
    worker: CoroutineWorker,
    notification: Notification,
    channel: NotificationChannelSystem.NotificationChannel,
  )

  fun cancel(notificationId: Int)
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, NotificationSystem::class)
@ContributesBinding(AppScope::class, NotificationPublisher::class)
class NotificationSystemInterface
  @Inject
  constructor(
    private val context: Context,
    private val notificationManager: NotificationManagerCompat,
    private val notificationChannelSystem: NotificationChannelSystem,
    private val intentProvider: IntentProvider,
  ) : NotificationSystem, NotificationPublisher {
    private var permissionRequestLauncher: ActivityResultLauncher<String>? = null

    override fun isNotificationPermissionGranted(): Boolean {
      val result = PermissionChecker.checkSelfPermission(context, POST_NOTIFICATIONS)
      return result == PackageManager.PERMISSION_GRANTED
    }

    override fun registerForPermissionResult(activity: ComponentActivity) {
      permissionRequestLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
        }
    }

    override fun requestPermission() {
      permissionRequestLauncher?.launch(POST_NOTIFICATIONS)
    }

    @SuppressLint("MissingPermission")
    override suspend fun post(
      notification: NotificationPublisher.Notification,
      channel: NotificationChannelSystem.NotificationChannel,
    ) {
      if (isNotificationPermissionGranted()) {
        notificationChannelSystem.notificationChannelId(channel)?.let { channelId ->
          notification.translate(channelId).run {
            notificationManager.notify(notification.id, this)
          }
        }
      }
    }

    override suspend fun post(
      worker: CoroutineWorker,
      notification: NotificationPublisher.Notification,
      channel: NotificationChannelSystem.NotificationChannel,
    ) {
      if (isNotificationPermissionGranted()) {
        notificationChannelSystem.notificationChannelId(channel)?.let { channelId ->
          worker.setForeground(
            ForegroundInfo(
              notification.id,
              notification.translate(channelId),
              ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            ),
          )
        }
      }
    }

    override fun cancel(notificationId: Int) {
      notificationManager.cancel(notificationId)
    }

    private fun NotificationPublisher.Notification.translate(channelId: String): Notification {
      return NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(title)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(intentProvider.openApp)
        .setAutoCancel(true)
        .setSound(sound?.uri)
        .setOngoing(ongoing)
        .build()
    }
  }
