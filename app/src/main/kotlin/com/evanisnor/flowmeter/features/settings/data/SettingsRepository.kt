package com.evanisnor.flowmeter.features.settings.data

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.di.SingleIn
import com.evanisnor.flowmeter.system.RingtoneSystem
import com.evanisnor.flowmeter.system.RingtoneSystem.RingtoneSound
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface SettingsRepository {

  suspend fun saveSessionStartSound(sound: RingtoneSound)
  suspend fun getSessionStartSound(): RingtoneSound

  suspend fun saveBreakIsOverSound(sound: RingtoneSound)
  suspend fun getBreakIsOverSound(): RingtoneSound

  suspend fun saveBreakIsOverVibrate(vibrate: Boolean)
  suspend fun getBreakIsOverVibrate(): Boolean

  suspend fun saveDebugQuickBreaks(debugEnableQuickBreaks: Boolean)
  suspend fun getDebugQuickBreaks(): Boolean

}

private const val KEY_SESSION_START_SOUND = "session_start_sound"
private const val KEY_BREAK_IS_OVER_SOUND = "break_is_over_sound"
private const val KEY_BREAK_IS_OVER_VIBRATE = "break_is_over_vibrate"
private const val DEBUG_KEY_ENABLE_QUICK_BREAKS = "debug_enable_quick_breaks"

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, SettingsRepository::class)
class SettingsRepositoryStore @Inject constructor(
  private val context: Context,
  private val ringtoneSystem: RingtoneSystem,
) : SettingsRepository {

  private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

  override suspend fun saveSessionStartSound(sound: RingtoneSound) {
    context.dataStore.edit { preferences ->
      preferences.writeRingtoneSound(KEY_SESSION_START_SOUND, sound)
    }
  }

  override suspend fun getSessionStartSound(): RingtoneSound {
    return context.dataStore.data.map { preferences ->
      preferences.readRingtoneSound(
        key = KEY_SESSION_START_SOUND,
        defaultValue = ringtoneSystem.getDefaultSound()
      )
    }.first()
  }

  override suspend fun saveBreakIsOverSound(sound: RingtoneSound) {
    context.dataStore.edit { preferences ->
      preferences.writeRingtoneSound(KEY_BREAK_IS_OVER_SOUND, sound)
    }
  }

  override suspend fun getBreakIsOverSound(): RingtoneSound {
    return context.dataStore.data.map { preferences ->
      preferences.readRingtoneSound(
        key = KEY_BREAK_IS_OVER_SOUND,
        defaultValue = ringtoneSystem.getDefaultSound()
      )
    }.first()
  }

  override suspend fun saveBreakIsOverVibrate(vibrate: Boolean) {
    context.dataStore.edit { preferences ->
      preferences.writeToggle(KEY_BREAK_IS_OVER_VIBRATE, vibrate)
    }
  }
  override suspend fun getBreakIsOverVibrate(): Boolean {
    return context.dataStore.data.map { preferences ->
      preferences.readToggle(KEY_BREAK_IS_OVER_VIBRATE, true)
    }.first()
  }

  override suspend fun saveDebugQuickBreaks(debugEnableQuickBreaks: Boolean) {
    context.dataStore.edit { preferences ->
      preferences.writeToggle(DEBUG_KEY_ENABLE_QUICK_BREAKS, debugEnableQuickBreaks)
    }
  }

  override suspend fun getDebugQuickBreaks(): Boolean {
    return context.dataStore.data.map { preferences ->
      preferences.readToggle(DEBUG_KEY_ENABLE_QUICK_BREAKS, false)
    }.first()
  }

  private fun MutablePreferences.writeRingtoneSound(key: String, sound: RingtoneSound) {
    this[stringPreferencesKey("${key}_name")] = sound.name
    this[stringPreferencesKey("${key}_uri")] = sound.uri.toString()
  }

  private fun Preferences.readRingtoneSound(
    key: String,
    defaultValue: RingtoneSound,
  ): RingtoneSound {
    val nameKey = stringPreferencesKey("${key}_name")
    val uriKey = stringPreferencesKey("${key}_uri")
    return if (contains(nameKey) && contains(uriKey)) {
      RingtoneSound(
        name = requireNotNull(get(nameKey)) { "Failed to retrieve $nameKey from datastore" },
        uri = requireNotNull(get(uriKey)) { "Failed to retrieve $uriKey from datastore" }.let { Uri.parse(it) }
      )
    } else {
      defaultValue
    }
  }

  private fun MutablePreferences.writeToggle(key: String, value: Boolean) {
    this[booleanPreferencesKey(key)] = value
  }

  private fun Preferences.readToggle(
    key: String,
    defaultValue: Boolean,
  ): Boolean = get( booleanPreferencesKey(key)) ?: defaultValue

}
