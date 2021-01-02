package com.well.sharedMobile.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@Suppress("unused") // used in iOS
class MutableSharedFlowWrapper<T>(
    replay: Int = 0,
): SharedFlow<T> {
    private val flow = MutableSharedFlow<T>(replay)

    fun emit(value: T) {
        CoroutineScope(Dispatchers.Default).launch {
            flow.emit(value)
        }
    }

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<T>) =
        flow.collect(collector)

    override val replayCache: List<T>
        get() = flow.replayCache
}