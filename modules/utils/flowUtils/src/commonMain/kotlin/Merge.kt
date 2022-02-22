package com.well.modules.utils.flowUtils

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
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

fun <T, C : Collection<T>> Flow<C>.filterNotEmpty(): Flow<C> = flow flow@{
    collect { value ->
        if (value.isNotEmpty()) {
            this@flow.emit(value)
        }
    }
}

inline fun <A, B, R> Flow<Pair<A, B>>.mapPair(crossinline transform: suspend (A, B) -> R) =
    map { transform(it.first, it.second) }

fun <T> Flow<T>.combineWithUnit(flow: Flow<Unit>): Flow<T> =
    combine(flow) { value, _ ->
        value
    }

inline fun <T> Flow<T>.collectIn(
    scope: CoroutineScope,
    crossinline action: suspend (value: T) -> Unit,
): Job = scope.launch {
    collect {
        action(it)
    }
}

fun <T, R> Flow<T>.mapProperty(property: kotlin.reflect.KProperty1<T, R>): Flow<R> =
    map { it.let(property) }

fun <E> Flow<Iterable<E>>.combineToSet(flow: Flow<Iterable<E>>): Flow<Set<E>> =
    combine(flow) { first, second ->
        first.toMutableSet().apply {
            addAll(second)
        }
    }

inline fun <T> Flow<T>.print(crossinline buildString: (T) -> String): Flow<T> =
    map {
        Napier.d(buildString(it))
        it
    }