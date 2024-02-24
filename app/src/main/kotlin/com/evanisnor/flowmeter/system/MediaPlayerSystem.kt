package com.evanisnor.flowmeter.system

import android.content.Context
import android.media.MediaPlayer
import com.evanisnor.flowmeter.di.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

interface MediaPlayerSystem {

  fun play (ringtoneSound: RingtoneSystem.RingtoneSound)

}

@ContributesBinding(AppScope::class, MediaPlayerSystem::class)
class MediaPlayerInterface @Inject constructor(
  private val context: Context,
  private val mediaPlayer: MediaPlayer,
) : MediaPlayerSystem {

  override fun play(ringtoneSound: RingtoneSystem.RingtoneSound) {
    try {
      mediaPlayer.apply {
        setDataSource(context, ringtoneSound.uri)
        prepare()
      }.start()
    } catch (e: IllegalStateException) {
      // TODO show a toast or something
    }
  }

}
