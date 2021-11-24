package com.well.modules.utils.flowUtils


import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow

class MutableSetStateFlow<T>(value: Set<T> = emptySet()): Flow<Set<T>> {
    private val flow = MutableStateFlow(value)
    val value: Set<T>
        get() = flow.value

    @OptIn(InternalCoroutinesApi::class)
    override suspend fun collect(collector: FlowCollector<Set<T>>) {
        flow.collect(collector)
    }

    fun add(element: T): Boolean =
        modify {
            add(element)
        }

    private fun modify(modification: MutableSet<T>.() -> Boolean): Boolean =
        flow.value.toMutableSet()
            .let { value ->
                value.modification()
                    .also { changed ->
                        if (changed) {
                            flow.value = value
                        }
                    }
            }
}