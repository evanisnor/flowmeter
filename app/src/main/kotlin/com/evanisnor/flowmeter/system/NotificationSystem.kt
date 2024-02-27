package com.evanisnor.flowmeter.system

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.media.AudioAttributes
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.PermissionChecker
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.evanisnor.flowmeter.R
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.di.SingleIn
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val NOTIFICATION_CHANNEL_ID = "com.evanisnor.flowmeter"
private const val POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"
private val VIBRATION_PATTERN =
  arrayOf(
    0L,
    1000L,
    200L,
    1000L,
    200L,
    1000L,
  ).toLongArray()

/**
 * Manage system notification integration
 */
interface NotificationSystem {
  data class NotificationChannelSettings(
    val sound: RingtoneSystem.RingtoneSound,
    val vibrate: Boolean,
  )

  suspend fun createNotificationChannel(settings: NotificationChannelSettings? = null)

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
    val sound: RingtoneSystem.RingtoneSound? = null,
  )

  suspend fun post(notification: Notification)

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
    private val audioAttributes: AudioAttributes,
    private val resources: Resources,
    private val intentProvider: IntentProvider,
  ) : NotificationSystem, NotificationPublisher {
    private var permissionRequestLauncher: ActivityResultLauncher<String>? = null
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
      name = "notifications",
    )
    private val notificationChannelIdKey = stringPreferencesKey("notification_channel_id")

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

    override suspend fun createNotificationChannel(
      settings: NotificationSystem.NotificationChannelSettings?,
    ) {
      val existingChannelId = notificationChannelId()
      if (existingChannelId != null && settings == null) {
        return
      }

      // Delete the previous Notification Channel so we can re-create it with the chosen sound.
      existingChannelId?.let { notificationManager.deleteNotificationChannel(it) }
      val newChannelId = NOTIFICATION_CHANNEL_ID + "|${System.currentTimeMillis()}"
      saveNotificationChannelId(newChannelId)

      NotificationChannelCompat.Builder(
        newChannelId,
        NotificationManagerCompat.IMPORTANCE_HIGH,
      )
        .setName(resources.getString(R.string.notification_channel_session))
        .setDescription(resources.getString(R.string.notification_channel_session_description))
        .setVibrationPattern(VIBRATION_PATTERN)
        .setVibrationEnabled(settings?.vibrate ?: true)
        .setSound(settings?.sound?.uri, audioAttributes)
        .build().let {
          notificationManager.createNotificationChannel(it)
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun post(notification: NotificationPublisher.Notification) {
      val channelId = notificationChannelId()
      if (isNotificationPermissionGranted() && channelId != null) {
        NotificationCompat.Builder(context, channelId)
          .setSmallIcon(R.drawable.ic_launcher_foreground)
          .setContentTitle(notification.title)
          .setPriority(NotificationCompat.PRIORITY_HIGH)
          .setContentIntent(intentProvider.openApp)
          .setAutoCancel(true)
          .setSound(notification.sound?.uri)
          .build().run {
            notificationManager.notify(notification.id, this)
          }
      }
    }

    override fun cancel(notificationId: Int) {
      notificationManager.cancel(notificationId)
    }

    private suspend fun notificationChannelId() =
      context.dataStore.data.map { preferences ->
        preferences[notificationChannelIdKey]
      }.first()

    private suspend fun saveNotificationChannelId(id: String) {
      context.dataStore.edit { preferences ->
        preferences[notificationChannelIdKey] = id
      }
    }
  }
