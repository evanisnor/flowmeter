package com.evanisnor.flowmeter.system

import android.content.Context
import android.media.MediaPlayer
import com.evanisnor.flowmeter.di.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import javax.inject.Provider

interface MediaPlayerSystem {

  fun play(ringtoneSound: RingtoneSystem.RingtoneSound)

}

@ContributesBinding(AppScope::class, MediaPlayerSystem::class)
class MediaPlayerInterface @Inject constructor(
  private val context: Context,
  private val mediaPlayer: Provider<MediaPlayer>,
) : MediaPlayerSystem {

  override fun play(ringtoneSound: RingtoneSystem.RingtoneSound) {
    mediaPlayer.get().apply {
      setDataSource(context, ringtoneSound.uri)
      prepare()
      setOnCompletionListener {
        release()
      }
    }.start()
  }

}
