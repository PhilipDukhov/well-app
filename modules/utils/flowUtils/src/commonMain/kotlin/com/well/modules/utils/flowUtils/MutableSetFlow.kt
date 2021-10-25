package com.well.modules.utils.flowUtils


import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow

class MutableSetFlow<T>(value: Set<T> = emptySet()): Flow<Set<T>> {
    private val flow = MutableStateFlow(value)
    val value: Set<T>
        get() = flow.value

    @OptIn(InternalCoroutinesApi::class)
    override suspend fun collect(collector: FlowCollector<Set<T>>) {
        flow.collect(collector)
    }

    suspend fun add(element: T): Boolean =
        modify {
            add(element)
        }

    private suspend fun modify(modification: MutableSet<T>.() -> Boolean): Boolean =
        flow.value.toMutableSet()
            .let { value ->
                value.modification()
                    .also { changed ->
                        if (changed) {
                            flow.emit(value)
                        }
                    }
            }
}