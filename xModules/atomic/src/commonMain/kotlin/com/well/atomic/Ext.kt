package com.well.atomic

fun <T> AtomicMutableList<(T) -> Unit>.notifyAll(t: T) {
    forEach { listener -> listener.invoke(t) }
}

fun <T, E: (T) -> Unit> AtomicMutableList<E>.addListenerAndMakeCloseable(listener: E): Closeable {
    add(listener)
    return object : Closeable {
        override fun close() {
            val index = indexOf(listener)
            if (index != -1) {
                removeAt(index)
            }
        }
    }
}