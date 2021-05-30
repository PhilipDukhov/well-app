package com.well.modules.atomic

import kotlin.reflect.KProperty

class AtomicLateInitRef<T> {
    private var ref by AtomicRef<T?>(null)

    var value: T
        get() = ref!!
        set(value) {
            ref = value
        }

    inline operator fun getValue(thisObj: Any?, property: KProperty<*>): T = value
    inline operator fun setValue(thisObj: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}