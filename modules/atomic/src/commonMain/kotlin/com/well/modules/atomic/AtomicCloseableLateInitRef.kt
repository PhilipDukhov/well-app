package com.well.modules.atomic

import kotlin.reflect.KProperty

class AtomicCloseableLateInitRef<T: Closeable>() {
    private var ref by AtomicCloseableRef<T>()

    var value: T
        get() = ref!!
        set(value) {
            ref?.close()
            ref = value
        }

    inline operator fun getValue(thisObj: Any?, property: KProperty<*>): T = value
    inline operator fun setValue(thisObj: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}