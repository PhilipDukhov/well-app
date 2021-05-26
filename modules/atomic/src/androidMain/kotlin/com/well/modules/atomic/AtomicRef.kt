package com.well.modules.atomic

import kotlin.reflect.KProperty

actual class AtomicRef<T> actual constructor(actual var value: T) {
    actual inline operator fun getValue(thisObj: Any?, property: KProperty<*>): T = value
    actual inline operator fun setValue(thisObj: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}