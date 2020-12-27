package com.well.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

interface Closeable {
    fun close()
}

abstract class CloseableContainer: Closeable {
    private val closeables = mutableListOf<Closeable>()

    fun addCloseableChild(closeable: Closeable) =
        closeables.add(closeable)

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
    private var job: Job

    init {
        job = scope.launch {
            closeable = task()
        }
    }

    var closeable: Closeable? = null

    override fun close() {
        job.cancel()
        closeable?.close()
    }
}