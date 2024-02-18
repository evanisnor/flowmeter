package com.evanisnor.flowmeter

import android.app.Application
import com.squareup.anvil.annotations.MergeComponent


/**
 * Top-level component for [AppScope]
 */
@SingleIn(AppScope::class)
@MergeComponent(AppScope::class)
interface FlowmeterAppComponent

/**
 * App entry-point class.
 */
class FlowmeterApp : Application() {

  override fun onCreate() {
    super.onCreate()
    DaggerFlowmeterAppComponent.create()
  }
}
