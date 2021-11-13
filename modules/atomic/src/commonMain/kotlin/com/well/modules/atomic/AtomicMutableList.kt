package com.well.modules.atomic

class AtomicMutableList<E>(value: List<E>): AbstractList<E>() {
    constructor() : this(listOf())
    private val atomicReference = AtomicRef(value)

    fun add(element: E, index: Int = count()) =
        modify(+1) {
            add(index, element)
        }
    fun addAll(elements: Collection<E>) =
        modify(+1) {
            addAll(elements)
        }

    fun remove(element: E) =
        modify(-1) {
            remove(element)
        }

    fun clear() =
        modify(-size) {
            clear()
        }

    fun removeAt(index: Int): E =
        modify(-1) {
            removeAt(index)
        }

    fun removeAll(predicate: (E) -> Boolean): Boolean =
        modify(-1) {
            removeAll(predicate)
        }

    fun set(index: Int, element: E): E =
        modify(0) {
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

    private fun <R> modify(capacityDiff: Int, block: ArrayList<E>.() -> R): R {
        val newValue = ArrayList<E>(size + capacityDiff)
        newValue.addAll(this)
        val result = block(newValue)
        atomicReference.value = newValue
        return result
    }
}