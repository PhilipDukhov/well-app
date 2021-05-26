package com.well.modules.atomic

import kotlin.native.concurrent.AtomicReference
import kotlin.reflect.KProperty

actual class AtomicRef<T> actual constructor(value: T) {
    private val atomicRef = AtomicReference(value.freeze())
    actual var value: T
        get() = atomicRef.value
        set(value) {
            atomicRef.value = value.freeze()
        }

    actual inline operator fun getValue(thisObj: Any?, property: KProperty<*>): T = value
    actual inline operator fun setValue(thisObj: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

