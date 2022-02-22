package com.well.modules.utils.kotlinUtils

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

inline fun <A, B, RB> Pair<A, B>.mapSecond(
    transformB: (B) -> RB,
) = map({ it }, transformB)

fun <E> Set<E>.removing(element: E): Set<E> = toMutableSet().apply { remove(element) }

inline fun <A, B, RA, RB> Pair<A, B>.mapNotNull(
    transformA: (A) -> RA?,
    transformB: (B) -> RB?,
): Pair<RA, RB>? {
    val f = transformA(first)
    val s = transformB(second)
    return if (f == null || s == null) null else f to s
}

infix fun <S, E> S.toFilterNotNull(that: Set<E?>): Pair<S, Set<E>> =
    this to that.filterNotNull().toSet()

suspend fun <R> tryF(
    block: suspend () -> R,
    catch: (t: Throwable) -> Unit,
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

fun <F, S> Collection<Pair<F, S>>.forEachNamed(block: (F, S) -> Unit) =
    forEach { it.letNamed(block) }

inline fun <T> ifTrueOrNull(condition: Boolean, block: () -> T): T? {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return if (condition) block() else null
}

inline fun <T, R> T.letIfTrueOrNull(condition: Boolean, block: (T) -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return if (condition) block(this) else null
}
