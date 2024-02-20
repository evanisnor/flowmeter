package com.evanisnor.flowmeter.system

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.PermissionChecker
import com.evanisnor.flowmeter.R
import com.evanisnor.flowmeter.di.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

private const val NOTIFICATION_CHANNEL_ID = "com.evanisnor.flowmeter"
private const val POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"
private val VIBRATION_PATTERN = arrayOf(
  0L,
  1000L,
  100L,
  1000L,
  100L,
  1000L,
  100L,
  1000L,
).toLongArray()

/**
 * Manage system notification integration
 */
interface NotificationSystem {
  fun createNotificationChannel()
  fun isNotificationPermissionGranted(): Boolean
  fun requestNotificationPermission(activity: ComponentActivity)
}

/**
 * Send notifications to the user
 */
interface NotificationPublisher {
  data class Notification(
    val id: Int,
    val title: String,
  )

  fun post(notification: Notification)
  fun cancel(notificationId: Int)
}


@ContributesBinding(AppScope::class, NotificationSystem::class)
@ContributesBinding(AppScope::class, NotificationPublisher::class)
class NotificationSystemInterface @Inject constructor(
  private val context: Context,
  private val notificationManager: NotificationManagerCompat,
  private val resources: Resources,
  private val intentProvider: IntentProvider,
) : NotificationSystem, NotificationPublisher {

  override fun isNotificationPermissionGranted(): Boolean {
    val result = PermissionChecker.checkSelfPermission(context, POST_NOTIFICATIONS)
    return result == PackageManager.PERMISSION_GRANTED
  }

  override fun requestNotificationPermission(activity: ComponentActivity) {
    activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
    }.launch(POST_NOTIFICATIONS)
  }

  override fun createNotificationChannel() {
    val channel = NotificationChannelCompat.Builder(
      NOTIFICATION_CHANNEL_ID,
      NotificationManagerCompat.IMPORTANCE_HIGH,
    )
      .setName(resources.getString(R.string.notification_channel_session))
      .setDescription(resources.getString(R.string.notification_channel_session_description))
      .setVibrationPattern(VIBRATION_PATTERN)
      .build()
    notificationManager.createNotificationChannel(channel)
  }

  @SuppressLint("MissingPermission")
  override fun post(notification: NotificationPublisher.Notification) {
    if (isNotificationPermissionGranted()) {
      NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(notification.title)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(intentProvider.openApp)
        .setVibrate(VIBRATION_PATTERN)
        .setAutoCancel(true)
        .build().run {
          notificationManager.notify(notification.id, this)
        }
    }
  }

  override fun cancel(notificationId: Int) {
    notificationManager.cancel(notificationId)
  }


}
