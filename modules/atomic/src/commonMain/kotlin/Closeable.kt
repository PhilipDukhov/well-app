package com.well.modules.atomic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

interface Closeable {
    fun close()
}

open class CloseableContainer : Closeable {
    private val closeables = AtomicMutableList<Closeable>()

    fun addCloseableChild(closeable: Closeable) =
        closeables.add(closeable)

    fun finalize() =
        close()

    override fun close() {
        closeables
            .dropAll()
            .forEach(Closeable::close)
    }
}

fun Job.asCloseable() = object : Closeable {
    override fun close() {
        if (isActive) {
            cancel()
        }
    }
}

fun CoroutineScope.asCloseable() = object : Closeable {
    override fun close() {
        cancel()
    }
}

class CloseableFuture(
    scope: CoroutineScope,
    task: suspend () -> Closeable,
) : Closeable {
    private var closeable by AtomicRef<Closeable?>(null)

    init {
        closeable = scope.launch {
            closeable = task().freeze()
        }.asCloseable().freeze()
    }

    override fun close() {
        closeable?.close()
    }

    override fun toString(): String =
        closeable.toString()
}