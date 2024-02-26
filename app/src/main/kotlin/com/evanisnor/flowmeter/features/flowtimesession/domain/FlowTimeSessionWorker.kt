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
 * Convenience function for launching [FlowTimeSessionUseCase]
 */
suspend fun WorkManagerSystem.startFlowTimeSession() : FlowTimeSessionUseCase {
  return if (isRunning(FlowTimeSessionWorker::class)) {
    locate(FlowTimeSessionWorker::class)
  } else {
    runOnce(FlowTimeSessionWorker::class)
  }
}

class FlowTimeSessionWorker @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted workerParameters: WorkerParameters,
  private val flowTimeSessionUseCase: FlowTimeSessionUseCase,
  private val registrar: WorkerRegistrar,
) : CoroutineWorker(context, workerParameters), FlowTimeSessionUseCase {

  private val work: Mutex = Mutex(locked = true)

  override suspend fun beginFlowSession() = flowTimeSessionUseCase.beginFlowSession()
  override suspend fun beginTakeABreak() = flowTimeSessionUseCase.beginTakeABreak()
  override suspend fun collect(collector: FlowCollector<FlowTimeSessionUseCase.FlowState>) = flowTimeSessionUseCase.collect(collector)

  override fun stop() {
    flowTimeSessionUseCase.stop()
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
  interface Factory : WorkerFactory<FlowTimeSessionWorker>

}

@Module
@ContributesTo(AppScope::class)
interface FlowTimeSessionWorkerFactoryModule {

  @Binds
  @IntoMap
  @WorkerKey(FlowTimeSessionWorker::class)
  @JvmSuppressWildcards
  fun bind(factory: FlowTimeSessionWorker.Factory) : WorkerFactory<*>

}
