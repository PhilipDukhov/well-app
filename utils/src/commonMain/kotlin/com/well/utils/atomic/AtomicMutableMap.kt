package com.well.utils.atomic

class AtomicMutableMap<K, V>(value: Map<K, V>): AbstractMutableMap<K, V>() {
    constructor() : this(mapOf())
    private val atomicReference = AtomicRef(value)

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = atomicReference.value.toMutableMap().entries

    override fun put(
        key: K,
        value: V
    ): V? {
        val newValue = atomicReference.value.toMutableMap()
        val res = newValue.put(key, value)
        atomicReference.value = newValue
        return res
    }
}