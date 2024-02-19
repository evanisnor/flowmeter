package com.evanisnor.flowmeter.di

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class InjectWith(@Suppress("unused") val scope: KClass<*>)


