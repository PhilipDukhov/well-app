package com.well.modules.atomic

import kotlin.reflect.KProperty

expect class AtomicRef<T>(value: T) {
    var value: T

    inline operator fun getValue(thisObj: Any?, property: KProperty<*>): T
    inline operator fun setValue(thisObj: Any?, property: KProperty<*>, value: T)
}

@Suppress("FunctionName")
fun <T> AtomicRef(): AtomicRef<T?> = AtomicRef(null)
