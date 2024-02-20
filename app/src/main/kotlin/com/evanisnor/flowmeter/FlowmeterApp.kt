package com.evanisnor.flowmeter

import android.app.Application
import android.content.Context
import androidx.activity.ComponentActivity
import com.evanisnor.flowmeter.di.AnvilInjector
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.di.SingleIn
import com.evanisnor.flowmeter.system.NotificationSystem
import com.squareup.anvil.annotations.MergeComponent
import dagger.BindsInstance
import dagger.Component


/**
 * Top-level component for [AppScope]
 */
@SingleIn(AppScope::class)
@MergeComponent(AppScope::class)
interface FlowmeterAppComponent {
  fun activityInjectors(): Map<Class<out ComponentActivity>, AnvilInjector<*>>
  fun notificationSystem() : NotificationSystem

  @Component.Builder
  interface Builder {
    @BindsInstance fun context(context: Context) : Builder
    fun build(): FlowmeterAppComponent
  }
}

/**
 * App entry-point class.
 */
class FlowmeterApp : Application() {

  private val appComponent: FlowmeterAppComponent = DaggerFlowmeterAppComponent.builder().context(this).build()

  override fun onCreate() {
    super.onCreate()
    appComponent.notificationSystem().createNotificationChannel()
  }

  fun inject(activity: MainActivity) {
    appComponent.activityInjectors()[activity::class.java]?.let {
      it as MainActivityAnvilInjector
      it.inject(activity)
    }
  }
}
