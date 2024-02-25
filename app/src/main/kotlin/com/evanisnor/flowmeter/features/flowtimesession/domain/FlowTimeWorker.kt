package com.evanisnor.flowmeter.features.flowtimesession.domain

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.di.WorkerFactory
import com.evanisnor.flowmeter.di.WorkerKey
import com.evanisnor.flowmeter.system.WorkManagerSystem
import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.multibindings.IntoMap
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.minutes

/**
 * Convenience function for launching [FlowTimeWorker]
 */
suspend fun WorkManagerSystem.startFlowTime() : FlowTimeSession {
  runOnce(FlowTimeWorker::class)
  return locate(FlowTimeWorker::class)
}

/**
 * Worker wrapper for [FlowTimeSession] so it can run persistently.
 */
class FlowTimeWorker @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted workerParameters: WorkerParameters,
  private val flowTimeSession: FlowTimeSession,
  private val workManagerSystem: WorkManagerSystem,
) : CoroutineWorker(context, workerParameters), FlowTimeSession {

  private val isRunning: AtomicBoolean = AtomicBoolean(false)

  override suspend fun collect(collector: FlowCollector<FlowTimeSession.State>) = flowTimeSession.collect(collector)

  override fun stop() {
    flowTimeSession.stop()
    isRunning.set(false)
  }

  override suspend fun doWork(): Result {
    workManagerSystem.register(this)

    // Run with a delay until stop() is called
    isRunning.set(true)
    while (isRunning.get()) {
      delay(1.minutes)
    }

    return Result.success().also {
      workManagerSystem.unregister(this)
    }
  }

  @AssistedFactory
  interface Factory : WorkerFactory<FlowTimeWorker>

}

@Module
@ContributesTo(AppScope::class)
interface FlowTimeWorkerFactoryModule {

  @Binds
  @IntoMap
  @WorkerKey(FlowTimeWorker::class)
  @JvmSuppressWildcards
  fun bind(factory: FlowTimeWorker.Factory) : WorkerFactory<*>

}
