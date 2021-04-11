package com.well.modules.atomic

class AtomicMutableMap<K, V>(value: Map<K, V>) : AbstractMap<K, V>() {
    constructor() : this(mapOf())

    private val atomicReference = AtomicRef(value)

    override val entries: Set<Map.Entry<K, V>>
        get() = atomicReference.value.entries

    override val values: Collection<V>
        get() = entries.map { it.value }

    operator fun set(
        key: K,
        value: V
    ) = put(key, value)

    fun remove(key: K) = update {
        remove(key)
    }

    fun put(
        key: K,
        value: V
    ): V? = update {
        put(key, value)
    }

    inline fun getOrPut(
        key: K,
        defaultValue: () -> V
    ): V {
        val value = get(key)
        return if (value == null) {
            val answer = defaultValue()
            put(key, answer)
            answer
        } else {
            value
        }
    }

    private fun <R> update(block: MutableMap<K, V>.() -> R): R {
        val newValue = atomicReference.value.toMutableMap()
        val res = block(newValue)
        atomicReference.value = newValue
        return res
    }
}