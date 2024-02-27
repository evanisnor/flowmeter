package com.evanisnor.flowmeter

import android.content.Context
import androidx.activity.ComponentActivity
import com.evanisnor.flowmeter.di.AnvilInjector
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.di.SingleIn
import com.evanisnor.flowmeter.system.NotificationSystem
import com.evanisnor.flowmeter.system.WorkerFactoryFactory
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

  fun workerFactoryFactory(): WorkerFactoryFactory

  fun notificationSystem(): NotificationSystem

  @Component.Builder
  interface Builder {
    @BindsInstance
    fun context(context: Context): Builder

    fun build(): FlowmeterAppComponent
  }
}
