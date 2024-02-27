package com.evanisnor.flowmeter.system

import android.content.Context
import android.content.res.Resources
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.provider.MediaStore.Audio
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import com.evanisnor.flowmeter.di.AppScope
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MainScope

@Module
@ContributesTo(AppScope::class)
object SystemModule {

  @Provides
  @MainScope
  fun mainScope(): CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

  @Provides
  fun resources(context: Context): Resources = context.resources

  @Provides
  fun ringtoneManager(context: Context): RingtoneManager = RingtoneManager(context)

  @Provides
  fun notificationManager(context: Context): NotificationManagerCompat =
    NotificationManagerCompat.from(context)

  @Provides
  fun audioAttributes(): AudioAttributes = AudioAttributes.Builder()
    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
    .setUsage(AudioAttributes.USAGE_MEDIA)
    .build()

  @Provides
  fun mediaPlayer(audioAttributes: AudioAttributes) : MediaPlayer =
    MediaPlayer().apply {
      setAudioAttributes(audioAttributes)
    }

  @Provides
  fun workManager(context: Context) : WorkManager = WorkManager.getInstance(context)

}
