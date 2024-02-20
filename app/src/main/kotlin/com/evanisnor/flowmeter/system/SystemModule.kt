package com.evanisnor.flowmeter.system

import android.content.Context
import android.content.res.Resources
import android.media.RingtoneManager
import androidx.core.app.NotificationManagerCompat
import com.evanisnor.flowmeter.di.AppScope
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides

@Module
@ContributesTo(AppScope::class)
object SystemModule {

  @Provides
  fun resources(context: Context): Resources = context.resources

  @Provides
  fun ringtoneManager(context: Context): RingtoneManager = RingtoneManager(context)

  @Provides
  fun notificationManager(context: Context): NotificationManagerCompat =
    NotificationManagerCompat.from(context)

}
