package com.well.utils.atomic

expect class AtomicRef<T>(value: T) {
    var value: T
}

fun <T> AtomicRef<T>.getAndSet(newValue: T): T {
    val result = value
    value = newValue
    return result
}