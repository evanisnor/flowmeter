package com.evanisnor.flowmeter.system

import android.content.Context
import android.content.res.Resources
import android.media.AudioAttributes
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
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

private const val NOTIFICATION_CHANNEL_PREFIX = "com.evanisnor.flowmeter"
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
interface NotificationChannelSystem {
  sealed interface NotificationChannel {
    val name: String
    val importance: Int
    val useDefaultSound: Boolean
    val defaultVibrate: Boolean

    data object FlowSessionNotificationChannel : NotificationChannel {
      override val name: String = "$NOTIFICATION_CHANNEL_PREFIX:channel:flow_session"
      override val importance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT
      override val useDefaultSound: Boolean = false
      override val defaultVibrate: Boolean = false
    }

    data object TakingABreakNotificationChannel : NotificationChannel {
      override val name: String = "$NOTIFICATION_CHANNEL_PREFIX:channel:taking_a_break"
      override val importance: Int = NotificationManagerCompat.IMPORTANCE_DEFAULT
      override val useDefaultSound: Boolean = false
      override val defaultVibrate: Boolean = false
    }

    data object BreakIsOverNotificationChannel : NotificationChannel {
      override val name: String = "$NOTIFICATION_CHANNEL_PREFIX:channel:break_is_over"
      override val importance: Int = NotificationManagerCompat.IMPORTANCE_HIGH
      override val useDefaultSound: Boolean = true
      override val defaultVibrate: Boolean = true
    }
  }

  data class NotificationChannelSettings(
    val sound: RingtoneSystem.RingtoneSound,
    val vibrate: Boolean,
  )

  suspend fun notificationChannelId(channel: NotificationChannel): String?

  suspend fun createNotificationChannel(
    channel: NotificationChannel,
    settings: NotificationChannelSettings? = null,
  )
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, NotificationChannelSystem::class)
class NotificationChannelSystemInterface
  @Inject
  constructor(
    private val context: Context,
    private val notificationManager: NotificationManagerCompat,
    private val ringtoneSystem: RingtoneSystem,
    private val audioAttributes: AudioAttributes,
    private val resources: Resources,
  ) : NotificationChannelSystem {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
      name = "notifications",
    )

    override suspend fun notificationChannelId(
      channel: NotificationChannelSystem.NotificationChannel,
    ) = context.dataStore.data.map { preferences ->
      preferences[stringPreferencesKey(channel.name)]
    }.first()

    override suspend fun createNotificationChannel(
      channel: NotificationChannelSystem.NotificationChannel,
      settings: NotificationChannelSystem.NotificationChannelSettings?,
    ) {
      val existingChannelId = notificationChannelId(channel)
      if (existingChannelId != null && settings == null) {
        return
      }

      // Delete the previous Notification Channel so we can re-create it with the chosen sound.
      existingChannelId?.let { notificationManager.deleteNotificationChannel(it) }
      val newChannelId = channel.name + "|${System.currentTimeMillis()}"
      saveNotificationChannelId(channel, newChannelId)

      val sound =
        settings?.sound?.uri ?: if (channel.useDefaultSound) {
          ringtoneSystem.getDefaultSound().uri
        } else {
          null
        }
      val vibrate = settings?.vibrate ?: channel.defaultVibrate

      NotificationChannelCompat.Builder(newChannelId, channel.importance)
        .setName(resources.getString(R.string.notification_channel_session))
        .setDescription(resources.getString(R.string.notification_channel_session_description))
        .setSound(sound, audioAttributes)
        .setVibrationEnabled(vibrate)
        .apply {
          if (vibrate) {
            setVibrationPattern(VIBRATION_PATTERN)
          }
        }
        .build().let {
          notificationManager.createNotificationChannel(it)
        }
    }

    private suspend fun saveNotificationChannelId(
      channel: NotificationChannelSystem.NotificationChannel,
      id: String,
    ) {
      context.dataStore.edit { preferences ->
        preferences[stringPreferencesKey(channel.name)] = id
      }
    }
  }
