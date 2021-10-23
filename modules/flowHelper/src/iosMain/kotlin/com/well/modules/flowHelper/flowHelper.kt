package com.well.modules.flowHelper

import com.well.modules.atomic.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun <T> StateFlow<T>.onChange(provideNewState: ((T) -> Unit)): Closeable {
    val job = Job()
    onEach {
        provideNewState(it)
    }.launchIn(CoroutineScope(Dispatchers.Main + job))
    return object : Closeable {
        override fun close() {
            job.cancel()
        }
    }
}