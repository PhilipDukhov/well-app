package com.well.modules.atomic

class AtomicMutableList<T>(value: List<T>): AbstractList<T>() {
    constructor() : this(listOf())
    private var atomicReference by AtomicRef(value)

    fun add(element: T, index: Int = count()) =
        modify(+1) {
            add(index, element)
        }

    fun remove(t: T) =
        modify(-1) {
            remove(t)
        }

    fun clear() =
        modify(-size) {
            clear()
        }

    fun removeAt(index: Int): T =
        modify(-1) {
            removeAt(index)
        }

    fun set(index: Int, element: T): T =
        modify(0) {
            set(index, element)
        }

    fun dropAll(): List<T> {
        val result = atomicReference
        atomicReference = listOf()
        return result
    }

    override val size: Int get() = atomicReference.size
    override fun isEmpty(): Boolean = atomicReference.isEmpty()
    override fun contains(element: T): Boolean = atomicReference.contains(element)
    override fun get(index: Int): T = atomicReference[index]
    override fun indexOf(element: T): Int = atomicReference.indexOf(element)
    override fun lastIndexOf(element: T): Int = atomicReference.lastIndexOf(element)
    override fun iterator(): Iterator<T> = atomicReference.iterator()

    private fun <R> modify(capacityDiff: Int, block: ArrayList<T>.() -> R): R {
        val newValue = ArrayList<T>(size + capacityDiff)
        newValue.addAll(this)
        val result = block(newValue)
        atomicReference = newValue
        return result
    }
}