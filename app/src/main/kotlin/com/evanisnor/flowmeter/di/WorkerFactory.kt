package com.evanisnor.flowmeter.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import dagger.MapKey
import kotlin.reflect.KClass


interface WorkerFactory<T: ListenableWorker> {
  fun create(context: Context, workerParameters: WorkerParameters): T
}

@Target(AnnotationTarget.FUNCTION)
@MapKey
@Retention(AnnotationRetention.RUNTIME)
annotation class WorkerKey(@Suppress("unused") val worker: KClass<out ListenableWorker>)

