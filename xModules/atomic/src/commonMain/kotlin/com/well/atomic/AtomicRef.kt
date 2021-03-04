package com.well.atomic

expect class AtomicRef<T>(value: T) {
    var value: T
}

fun <T> AtomicRef<T>.getAndSet(newValue: T): T {
    val result = value
    value = newValue
    return result
}

@Suppress("FunctionName")
fun <T> AtomicRef(): AtomicRef<T?> = AtomicRef(null)

fun AtomicRef<Int>.inc(): Int {
    val newValue = value + 1
    value = newValue
    return newValue
}