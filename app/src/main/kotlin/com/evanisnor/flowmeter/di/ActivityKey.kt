package com.evanisnor.flowmeter.di

import androidx.activity.ComponentActivity
import dagger.MapKey
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@MapKey
@Retention(AnnotationRetention.RUNTIME)
annotation class ActivityKey(@Suppress("unused") val activity: KClass<out ComponentActivity>)
