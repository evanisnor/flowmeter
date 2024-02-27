package com.evanisnor.flowmeter.system

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.di.SingleIn
import com.evanisnor.flowmeter.di.WorkerFactory
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.CompletableDeferred
import timber.log.Timber
import javax.inject.Inject
import kotlin.reflect.KClass

interface WorkManagerSystem {
  fun isRunning(worker: KClass<out ListenableWorker>): Boolean

  suspend fun <T : ListenableWorker> runOnce(worker: KClass<T>): T

  suspend fun <T : ListenableWorker> locate(worker: KClass<T>): T
}

interface WorkerRegistrar {
  fun <T : ListenableWorker> register(worker: T)

  fun <T : ListenableWorker> unregister(worker: T)
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, WorkManagerSystem::class)
@ContributesBinding(AppScope::class, WorkerRegistrar::class)
class WorkManagerSystemIntegration
  @Inject
  constructor(
    private val workManager: WorkManager,
  ) : WorkManagerSystem, WorkerRegistrar {
    private val workerMap: MutableMap<KClass<out ListenableWorker>, CompletableDeferred<ListenableWorker>> = mutableMapOf()

    override fun isRunning(worker: KClass<out ListenableWorker>): Boolean = workerMap.contains(worker)

    override suspend fun <T : ListenableWorker> runOnce(worker: KClass<T>): T {
      workManager.beginUniqueWork(
        worker.java.simpleName,
        ExistingWorkPolicy.REPLACE,
        OneTimeWorkRequest.Builder(worker.java).build(),
      ).enqueue()
      return locate(worker)
    }

    override fun <T : ListenableWorker> register(worker: T) {
      workerMap.getOrPut(worker::class, defaultValue = { CompletableDeferred() }).complete(worker)
      Timber.d("Registered Worker ${worker::class.simpleName}")
    }

    override fun <T : ListenableWorker> unregister(worker: T) {
      workerMap.remove(worker::class)
      Timber.d("Unregistered Worker ${worker::class.simpleName}")
    }

    override suspend fun <T : ListenableWorker> locate(worker: KClass<T>): T {
      val t0 = System.currentTimeMillis()
      @Suppress("UNCHECKED_CAST")
      return (workerMap.getOrPut(worker, defaultValue = { CompletableDeferred() }).await() as T).also {
        Timber.d("Located ${worker.simpleName} in ${System.currentTimeMillis() - t0}ms")
      }
    }
  }

/**
 * WorkerFactory for creating Workers.
 */
class WorkerFactoryFactory
  @Inject
  constructor(
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
