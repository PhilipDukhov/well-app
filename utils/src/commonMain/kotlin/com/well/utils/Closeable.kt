package com.well.utils

import com.well.utils.atomic.AtomicMutableList
import com.well.utils.atomic.AtomicRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

interface Closeable {
    fun close()
}

open class CloseableContainer: Closeable {
    private val closeables = AtomicMutableList<Closeable>()

    fun addCloseableChild(closeable: Closeable) =
        closeables.add(closeable)

    fun finalize() =
        close()

    override fun close() {
        closeables.forEach(Closeable::close)
        closeables.clear()
    }
}

fun Job.asCloseable() = object: Closeable {
    override fun close() {
        cancel()
    }
}

class CloseableFuture(
    scope: CoroutineScope,
    task: suspend () -> Closeable,
) : Closeable {
    private val closeable = AtomicRef<Closeable?>(null)

    init {
        closeable.value = scope.launch {
            closeable.value = task().freeze()
        }.asCloseable().freeze()
    }

    override fun close() {
        closeable.value?.close()
    }
}