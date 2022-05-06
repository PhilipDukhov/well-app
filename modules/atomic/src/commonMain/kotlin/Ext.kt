package com.well.modules.atomic

fun <E> AtomicMutableList<(E) -> Unit>.notifyAll(e: E) {
    forEach { listener -> listener.invoke(e) }
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