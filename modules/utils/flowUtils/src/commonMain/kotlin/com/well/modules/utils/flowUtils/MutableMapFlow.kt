package com.well.modules.utils.flowUtils

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow

class MutableMapFlow<K, V>(value: Map<K, V> = emptyMap()): Flow<Map<K, V>> {
    private val flow = MutableStateFlow(value)

    @OptIn(InternalCoroutinesApi::class)
    override suspend fun collect(collector: FlowCollector<Map<K, V>>) {
        flow.collect(collector)
    }

    suspend fun put(key: K, value: V): V? =
        flow.value.toMutableMap().let { map ->
            map.put(key, value)
                .also {
                    flow.emit(map)
                }
        }

    operator fun get(key: K): V? = flow.value[key]

    suspend fun remove(key: K) : V? =
        flow.value.toMutableMap()
            .let { value ->
                value.remove(key)
                    .also { oldValue ->
                        if (oldValue != null) {
                            flow.emit(value)
                        }
                    }
            }
}