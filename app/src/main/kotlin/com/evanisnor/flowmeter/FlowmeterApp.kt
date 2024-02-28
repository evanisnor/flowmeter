package com.evanisnor.flowmeter

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.Timber.DebugTree
import com.evanisnor.flowmeter.system.NotificationChannelSystem.NotificationChannel.FlowSessionNotificationChannel
import com.evanisnor.flowmeter.system.NotificationChannelSystem.NotificationChannel.BreakIsOverNotificationChannel

/**
 * App entry-point class.
 */
class FlowmeterApp : Application() {
  private val appComponent: FlowmeterAppComponent =
    DaggerFlowmeterAppComponent.builder().context(this).build()

  override fun onCreate() {
    super.onCreate()

    if (BuildConfig.DEBUG) {
      Timber.plant(DebugTree())
    }

    WorkManager.initialize(
      this@FlowmeterApp,
      Configuration.Builder().setWorkerFactory(appComponent.workerFactoryFactory()).build(),
    )
    WorkManager.getInstance(this).cancelAllWork()

    CoroutineScope(Dispatchers.Main).launch {
      with(appComponent.notificationChannelSystem()) {
        createNotificationChannel(FlowSessionNotificationChannel)
        createNotificationChannel(BreakIsOverNotificationChannel)
      }
    }
  }

  fun inject(activity: MainActivity) {
    appComponent.activityInjectors()[activity::class.java]?.let {
      it as MainActivityAnvilInjector
      it.inject(activity)
    }
  }
}
