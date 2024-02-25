package com.evanisnor.flowmeter.system

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.di.SingleIn
import com.evanisnor.flowmeter.di.WorkerFactory
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.CompletableDeferred
import java.lang.ref.WeakReference
import java.util.UUID
import javax.inject.Inject
import kotlin.reflect.KClass

interface WorkManagerSystem {
  fun <T : ListenableWorker> runOnce(worker: KClass<T>)
  fun <T: ListenableWorker> register(worker: T)
  fun <T: ListenableWorker> unregister(worker: T)
  suspend fun <T: ListenableWorker> locate(worker: KClass<T>) : T
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, WorkManagerSystem::class)
class WorkManagerSystemIntegration @Inject constructor(
  private val workManager: WorkManager
) : WorkManagerSystem {

  private val workerMap : MutableMap<KClass<out ListenableWorker>, CompletableDeferred<ListenableWorker>> = mutableMapOf()

  override fun <T : ListenableWorker> runOnce(worker: KClass<T>) {
    workManager.enqueue(OneTimeWorkRequest.Builder(worker.java).build())
  }

  override fun <T : ListenableWorker> register(worker: T) {
    workerMap.getOrPut(worker::class, defaultValue = { CompletableDeferred() }).complete(worker)
  }

  override fun <T : ListenableWorker> unregister(worker: T) {
    workerMap.remove(worker::class)
  }

  override suspend fun <T : ListenableWorker> locate(worker: KClass<T>): T {
    @Suppress("UNCHECKED_CAST")
    return workerMap.getOrPut(worker, defaultValue = { CompletableDeferred() }).await() as T
  }

}

/**
 * WorkerFactory for creating Workers.
 */
class WorkerFactoryFactory @Inject constructor(
  private val workerFactories: Map<Class<out ListenableWorker>, @JvmSuppressWildcards WorkerFactory<*>>,
) : androidx.work.WorkerFactory() {

  override fun createWorker(
    appContext: Context,
    workerClassName: String,
    workerParameters: WorkerParameters,
  ): ListenableWorker? {
    return workerFactories.entries.find {
      Class.forName(workerClassName).isAssignableFrom(it.key)
    }?.value?.create(appContext, workerParameters)
  }

}
