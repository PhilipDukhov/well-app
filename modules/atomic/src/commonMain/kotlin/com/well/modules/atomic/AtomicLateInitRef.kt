package com.well.modules.atomic

class AtomicLateInitRef<T> {
    private var ref by AtomicRef<T?>(null)

    var value: T
        get() = ref!!
        set(value) {
            ref = value
        }
}