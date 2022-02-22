package com.well.modules.utils.kotlinUtils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun<I> kotlin.reflect.KSuspendFunction1<I, Unit>.launchedIn(
    coroutineScope: CoroutineScope,
): (I) -> Unit = { arg ->
    coroutineScope.launch {
        invoke(arg)
    }
}

inline fun<T, R> kotlin.reflect.KFunction1<R, Unit>.map(
    crossinline transform: (T) -> R,
): (T) -> Unit = { arg ->
    invoke(transform(arg))
}

fun kotlin.reflect.KSuspendFunction0<Unit>.launchedIn(
    coroutineScope: CoroutineScope,
): () -> Unit = {
    coroutineScope.launch {
        invoke()
    }
}

inline fun<T> kotlin.reflect.KMutableProperty0<T?>.getOrFill(
    create: () -> T,
): T = get() ?: create().also(::set)