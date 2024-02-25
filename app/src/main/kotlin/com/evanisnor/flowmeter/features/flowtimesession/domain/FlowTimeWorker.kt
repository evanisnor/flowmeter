package com.evanisnor.flowmeter.features.flowtimesession.domain

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.di.WorkerFactory
import com.evanisnor.flowmeter.di.WorkerKey
import com.evanisnor.flowmeter.system.WorkManagerSystem
import com.evanisnor.flowmeter.system.WorkerRegistrar
import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.multibindings.IntoMap
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.sync.Mutex

/**
 * Convenience function for launching [FlowTimeWorker]
 */
suspend fun WorkManagerSystem.startFlowTime() : FlowTimeSession = runOnce(FlowTimeWorker::class)

/**
 * Worker wrapper for [FlowTimeSession] so it can run persistently.
 */
class FlowTimeWorker @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted workerParameters: WorkerParameters,
  private val flowTimeSession: FlowTimeSession,
  private val registrar: WorkerRegistrar,
) : CoroutineWorker(context, workerParameters), FlowTimeSession {

  private val work: Mutex = Mutex(locked = true)

  override suspend fun collect(collector: FlowCollector<FlowTimeSession.State>) = flowTimeSession.collect(collector)

  override fun stop() {
    flowTimeSession.stop()
    if (work.isLocked) {
      work.unlock()
    }
    registrar.unregister(this)
  }

  override suspend fun doWork(): Result {
    registrar.register(this)
    work.lock()
    return Result.success()
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
