package com.well.modules.utils.flowUtils


import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow

class MutableSetStateFlow<E>(value: Set<E> = emptySet()) : Flow<Set<E>> {
    private val flow = MutableStateFlow(value)
    val value: Set<E>
        get() = flow.value

    @OptIn(InternalCoroutinesApi::class)
    override suspend fun collect(collector: FlowCollector<Set<E>>) {
        flow.collect(collector)
    }

    fun add(element: E): Boolean =
        modify {
            add(element)
        }

    fun remove(element: E): Boolean =
        modify {
            remove(element)
        }

    fun removeAll(elements: Collection<E>): Boolean =
        modify {
            removeAll(elements.toSet())
        }

    fun removeAll(predicate: (E) -> Boolean): Boolean =
        modify {
            removeAll(predicate)
        }

    private fun modify(modification: MutableSet<E>.() -> Boolean): Boolean =
        flow.value.toMutableSet()
            .let { value ->
                value.modification()
                    .also {
                        flow.value = value
                    }
            }
}