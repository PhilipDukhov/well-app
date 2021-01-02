package com.well.utils.atomic

import com.well.utils.freeze
import kotlin.native.concurrent.AtomicReference

actual class AtomicRef<T> actual constructor(value: T) {
    private val atomicRef = AtomicReference(value.freeze())
    actual var value: T
        get() = atomicRef.value
        set(value) {
            atomicRef.value = value.freeze()
        }
}

