package com.well.modules.utils.flowUtils

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow

class MutableMapFlow<K, V>(value: Map<K, V> = emptyMap()): Flow<Map<K, V>> {
    private val flow = MutableStateFlow(value)
    val value: Map<K, V>
        get() = flow.value

    @OptIn(InternalCoroutinesApi::class)
    override suspend fun collect(collector: FlowCollector<Map<K, V>>) {
        flow.collect(collector)
    }

    fun put(key: K, value: V): V? =
        modify {
            put(key, value)
        }

    operator fun get(key: K): V? = flow.value[key]

    fun remove(key: K) : V? =
        modify {
            remove(key)
        }

    fun contains(key: K): Boolean = flow.value.contains(key)

    private fun<R> modify(modification: MutableMap<K, V>.() -> R?): R? =
        flow.value.toMutableMap()
            .let { value ->
                value.modification()
                    .also { result ->
                        if (result != null) {
                            flow.value = value
                        }
                    }
            }
}