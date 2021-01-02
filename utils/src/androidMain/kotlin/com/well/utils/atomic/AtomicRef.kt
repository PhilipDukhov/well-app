package com.well.utils.atomic

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.AtomicReferenceArray

actual class AtomicRef<T> actual constructor(value: T) {
    private val atomicReference = AtomicReference(value)

    actual var value: T
        get() = atomicReference.get()
        set(value) = atomicReference.set(value)
}