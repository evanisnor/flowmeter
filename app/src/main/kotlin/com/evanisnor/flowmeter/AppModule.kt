package com.evanisnor.flowmeter

import android.content.Context
import android.media.RingtoneManager
import com.evanisnor.flowmeter.di.AppScope
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@ContributesTo(AppScope::class)
object AppModule {

  @Provides
  fun backgroundScope() : CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

  @Provides
  fun ringtoneManager(context: Context) : RingtoneManager = RingtoneManager(context)

}
