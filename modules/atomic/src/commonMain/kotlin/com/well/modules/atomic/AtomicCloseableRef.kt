package com.well.modules.atomic

class AtomicCloseableRef {
    private var ref by AtomicRef<Closeable?>(null)

    var value: Closeable?
        get() = ref
        set(value) {
            ref?.close()
            ref = value
        }
}