package com.evanisnor.flowmeter.features.flowtimesession.domain

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.di.WorkerFactory
import com.evanisnor.flowmeter.di.WorkerKey
import com.evanisnor.flowmeter.system.NotificationPublisher
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.sync.Mutex

private const val FLOW_SESSION_FOREGROUND_NOTIFICATION_ID = 123156

/**
 * Convenience function for launching [FlowTimeSessionUseCase]
 */
suspend fun WorkManagerSystem.startFlowTimeSession(): FlowTimeSessionUseCase {
  return if (isRunning(FlowTimeSessionWorker::class)) {
    locate(FlowTimeSessionWorker::class)
  } else {
    runOnce(FlowTimeSessionWorker::class)
  }
}

class FlowTimeSessionWorker
  @AssistedInject
  constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val flowTimeSessionUseCase: FlowTimeSessionUseCase,
    private val registrar: WorkerRegistrar,
    private val notificationPublisher: NotificationPublisher,
  ) : CoroutineWorker(context, workerParameters), FlowTimeSessionUseCase {
    private val work: Mutex = Mutex(locked = true)

    override suspend fun beginFlowSession() = flowTimeSessionUseCase.beginFlowSession()

    override suspend fun beginTakeABreak() = flowTimeSessionUseCase.beginTakeABreak()

    override suspend fun collect(collector: FlowCollector<FlowTimeSessionUseCase.FlowState>) {
      flowTimeSessionUseCase.collectLatest { state ->
        // Intercept flow state so we can update the Foreground Notification
        when (state) {
          is FlowTimeSessionUseCase.FlowState.InTheFlow ->
            postForegroundNotification(
              state.duration,
            )
          else -> { /* State ignored */ }
        }
        collector.emit(state)
      }
    }

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

    private suspend fun postForegroundNotification(duration: String) {
      if (!work.isLocked) {
        return
      }
      notificationPublisher.post(
        worker = this@FlowTimeSessionWorker,
        notification =
          NotificationPublisher.Notification(
            id = FLOW_SESSION_FOREGROUND_NOTIFICATION_ID,
            title = "In the zone. $duration",
            priority = NotificationCompat.PRIORITY_DEFAULT,
            ongoing = true,
          ),
      )
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
  fun bind(factory: FlowTimeSessionWorker.Factory): WorkerFactory<*>
}
