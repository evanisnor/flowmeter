package com.evanisnor.flowmeter.system

import android.app.Activity
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.di.SingleIn
import com.evanisnor.flowmeter.system.RingtoneSystem.RingtoneSound
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

interface RingtoneInitializer {
  fun initialize(activity: Activity)
}

interface RingtoneSystem {

  data class RingtoneSound(
    val name: String,
    val uri: Uri,
  )

  fun getSounds(): List<RingtoneSound>

  fun getDefaultSound() : RingtoneSound

}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, RingtoneSystem::class)
@ContributesBinding(AppScope::class, RingtoneInitializer::class)
class RingtoneSystemInterface @Inject constructor() : RingtoneSystem, RingtoneInitializer {

  private var sounds: List<RingtoneSound> = emptyList()

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
  }

  override fun getSounds(): List<RingtoneSound> = sounds

  override fun getDefaultSound(): RingtoneSound = requireNotNull(sounds.firstOrNull()) {
    "RingtoneSystem has not been initialized"
  }

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


}
