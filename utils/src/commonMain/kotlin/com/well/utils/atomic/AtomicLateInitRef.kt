package com.well.utils.atomic

class AtomicLateInitRef<T> {
    private val ref = AtomicRef<T?>(null)

    var value: T
        get() = ref.value!!
        set(value) {
            ref.value = value
        }
}