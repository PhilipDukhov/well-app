package com.well.modules.atomic

class AtomicMutableList<E>(value: List<E>): AbstractList<E>() {
    constructor() : this(listOf())
    private val atomicReference = AtomicRef(value)

    fun add(element: E, index: Int = count()) =
        modify {
            add(index, element)
        }
    fun addAll(elements: Collection<E>) =
        modify {
            addAll(elements)
        }

    fun remove(element: E) =
        modify {
            remove(element)
        }

    fun clear() =
        modify {
            clear()
        }

    fun removeAt(index: Int): E =
        modify {
            removeAt(index)
        }

    fun removeAll(predicate: (E) -> Boolean): Boolean =
        modify {
            removeAll(predicate)
        }

    fun set(index: Int, element: E): E =
        modify {
            set(index, element)
        }

    fun dropAll(): List<E> {
        val result = atomicReference.value
        atomicReference.value = listOf()
        return result
    }

    override val size: Int get() = atomicReference.value.size
    override fun isEmpty(): Boolean = atomicReference.value.isEmpty()
    override fun contains(element: E): Boolean = atomicReference.value.contains(element)
    override fun get(index: Int): E = atomicReference.value[index]
    override fun indexOf(element: E): Int = atomicReference.value.indexOf(element)
    override fun lastIndexOf(element: E): Int = atomicReference.value.lastIndexOf(element)
    override fun iterator(): Iterator<E> = atomicReference.value.iterator()

    private fun <R> modify(block: MutableList<E>.() -> R): R {
        val newValue = toMutableList()
        val result = block(newValue)
        atomicReference.value = newValue
        return result
    }
}