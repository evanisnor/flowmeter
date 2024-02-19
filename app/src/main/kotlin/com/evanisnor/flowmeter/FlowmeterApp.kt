package com.evanisnor.flowmeter

import android.app.Application
import android.content.Context
import com.squareup.anvil.annotations.ContributesTo
import com.squareup.anvil.annotations.MergeComponent
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module


/**
 * Top-level component for [AppScope]
 */
@SingleIn(AppScope::class)
@MergeComponent(AppScope::class)
interface FlowmeterAppComponent {

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

  override fun onCreate() {
    super.onCreate()
    DaggerFlowmeterAppComponent.builder().context(this).build()
  }
}
