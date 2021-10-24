package com.well.modules.flowHelper

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

fun <T> Flow<Flow<T>>.flatMapLatest(): Flow<T> = flatMapLatest { it }

inline fun <T, R> Flow<Iterable<T>>.mapIterable(crossinline transform: (T) -> R): Flow<List<R>> =
    map { it.map(transform) }

inline fun <T> Flow<Iterable<T>>.filterIterable(crossinline predicate: (T) -> Boolean): Flow<List<T>> =
    map { it.filter(predicate) }

fun <T, C: Collection<T>> Flow<C>.filterNotEmpty(): Flow<C> = flow flow@{
    collect { value ->
        if (value.isNotEmpty()) {
            this@flow.emit(value)
        }
    }
}

fun <T> Flow<T>.combineToUnit(flow: Flow<Unit>): Flow<T> =
    combine(flow) { value, _ ->
        value
    }