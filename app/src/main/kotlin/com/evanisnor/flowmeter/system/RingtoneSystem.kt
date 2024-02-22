package com.evanisnor.flowmeter.system

import android.app.Activity
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Parcelable
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.di.SingleIn
import com.evanisnor.flowmeter.system.RingtoneSystem.RingtoneSound
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

interface RingtoneInitializer {
  fun initialize(activity: Activity)
}

interface RingtoneSystem {

  @Parcelize
  data class RingtoneSound(
    val name: String,
    val uri: Uri,
  ) : Parcelable

  fun getSounds(): List<RingtoneSound>

  fun getDefaultSound(): RingtoneSound

}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, RingtoneSystem::class)
@ContributesBinding(AppScope::class, RingtoneInitializer::class)
class RingtoneSystemInterface @Inject constructor() : RingtoneSystem, RingtoneInitializer {

  private var sounds: List<RingtoneSound> = emptyList()
  private lateinit var defaultNotificationSound: RingtoneSound

  /**
   * Can only extract available ringtones when [RingtoneManager] is initialized from an [Activity].
   */
  override fun initialize(activity: Activity) {
    sounds = try {
      RingtoneManager(activity).extractSounds(activity)
    } catch (e: Exception) {
      // TODO Protect our users from this terrible, terrible API.
      emptyList()
    }

    defaultNotificationSound = try {
      RingtoneManager(activity).getDefaultRingtone(RingtoneManager.TYPE_NOTIFICATION, activity)
    } catch (e: Exception) {
      requireNotNull(sounds.firstOrNull()) {
        "Failed to find default notification sound and catalog available ringtones."
      }
    }
  }

  override fun getSounds(): List<RingtoneSound> = sounds

  override fun getDefaultSound(): RingtoneSound = defaultNotificationSound

  /**
   * Why do you make me do this???
   */
  private fun RingtoneManager.extractSounds(context: Context): List<RingtoneSound> {
    setType(RingtoneManager.TYPE_ALL)
    val cursor = this.cursor
    return buildList {
      while (cursor.moveToNext()) {
        add(
          RingtoneSound(
            name = getRingtone(cursor.position).getTitle(context),
            uri = getRingtoneUri(cursor.position),
          )
        )
      }
    }
  }

  /**
   * this is dumb
   */
  private fun RingtoneManager.getDefaultRingtone(type: Int, context: Context): RingtoneSound {
    return RingtoneSound(
      name = getRingtone(type).getTitle(context),
      uri = getRingtoneUri(type),
    )
  }


}
