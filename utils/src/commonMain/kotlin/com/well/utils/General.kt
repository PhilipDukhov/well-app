package com.well.utils

expect fun <T> T.freeze(): T

internal fun <T> List<(T) -> Unit>.notifyAll(msg: T) = forEach { listener -> listener.invoke(msg) }

internal fun <T> MutableList<(T) -> Unit>.addListenerAndMakeCancelable(listener: (T) -> Unit): Closeable =
    listener.run {
        add(this)
        object : Closeable {
            override fun close() {
                remove(this@run)
            }
        }
    }