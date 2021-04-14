package com.well.modules.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ScreenStates(val empties: Array<String>, val features: Array<KClass<out Any>>)
