package com.well.modules.utils.flowUtils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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

fun <T> Flow<T>.combineWithUnit(flow: Flow<Unit>): Flow<T> =
    combine(flow) { value, _ ->
        value
    }

inline fun <T> Flow<T>.collectIn(
    scope: CoroutineScope,
    crossinline action: suspend (value: T) -> Unit,
): Job = scope.launch {
    collect(action)
}

inline fun <T, R> Flow<T>.mapProperty(property: kotlin.reflect.KProperty1<T, R>): Flow<R> =
    map { it.let(property) }
