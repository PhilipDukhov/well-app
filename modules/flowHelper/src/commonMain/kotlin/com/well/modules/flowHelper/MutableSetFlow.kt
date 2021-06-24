package com.well.modules.flowHelper


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform

class MutableSetFlow<T>(value: Set<T> = emptySet()): Flow<Set<T>> {
    private val flow = MutableStateFlow(value)
    val value: Set<T>
        get() = flow.value

    override suspend fun collect(collector: FlowCollector<Set<T>>) {
        flow.collect(collector)
    }

    suspend fun add(element: T): Boolean =
        modify {
            add(element)
        }

    suspend fun modify(modification: MutableSet<T>.() -> Boolean): Boolean =
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