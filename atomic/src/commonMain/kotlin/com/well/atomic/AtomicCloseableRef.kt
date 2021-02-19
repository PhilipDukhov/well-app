package com.well.atomic

class AtomicCloseableRef {
    private val ref = AtomicRef<Closeable?>(null)

    var value: Closeable?
        get() = ref.value
        set(value) {
            ref.value?.close()
            ref.value = value
        }
}