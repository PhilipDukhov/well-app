package com.well.modules.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun <A, B, RA, RB> Pair<A, B>.map(
    transformA: (A) -> RA,
    transformB: (B) -> RB,
): Pair<RA, RB> {
    val f = transformA(first)
    val s = transformB(second)
    return f to s
}

inline fun <A, B, RA, RB> Pair<A, B>.mapNotNull(
    transformA: ((A) -> RA?),
    transformB: (B) -> RB?,
): Pair<RA, RB>? {
    val f = transformA(first)
    val s = transformB(second)
    return if (f == null || s == null) null else f to s
}

infix fun <S, E> S.toSetOf(that: E?): Pair<S, Set<E>> =
    Pair(this, if (that != null) setOf(that) else emptySet())

infix fun <S, E> S.toFilterNotNull(that: Set<E?>): Pair<S, Set<E>> =
    this to that.filterNotNull().toSet()

infix fun <S, E> Pair<S, Set<E>>.plus(that: E?): Pair<S, Set<E>> =
    Pair(first, second + if (that != null) setOf(that) else emptySet())

fun <A, B> A.withEmptySet(): Pair<A, Set<B>> = Pair(this, setOf())

inline fun <reified T : Enum<T>> T.nextEnumValue(): T =
    enumValues<T>().run {
        this[(ordinal + 1) % this.count()]
    }

@Suppress("FunctionName")
fun <T> MutableStateFlow(): MutableStateFlow<T?> = MutableStateFlow(null)

suspend fun <R> tryF(
    block: suspend () -> R,
    catch: (t: Throwable) -> Unit
): R? = try {
    block()
} catch (t: Throwable) {
    catch(t)
    null
}

suspend fun <R> tryF(
    vararg catchers: (t: Throwable) -> Boolean,
    block: suspend () -> R,
): R = try {
    block()
} catch (t: Throwable) {
    catchers.firstOrNull { it(t) }
    throw t
}

inline fun <F, S, R> Pair<F, S>.letNamed(block: (F, S) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return block(first, second)
}

fun <F, S> Collection<Pair<F, S>>.forEachNamed(block: (F, S) -> Unit) = forEach { it.letNamed(block) }

