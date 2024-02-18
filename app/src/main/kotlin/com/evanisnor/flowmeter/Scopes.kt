package com.evanisnor.flowmeter

import javax.inject.Scope
import kotlin.reflect.KClass

/**
 * Dependency lifecycle scope for the app
 */
abstract class AppScope private constructor()

/**
 * Singleton annotation
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class SingleIn(val scope: KClass<*>)

