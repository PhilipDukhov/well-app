package com.well.utils

expect fun <T> T.freeze(): T

inline fun <A, B, RA, RB> Pair<A, B>.map(
    transformA: ((A) -> RA),
    transformB: (B) -> RB,
) : Pair<RA, RB> {
    val f = transformA(first)
    val s = transformB(second)
    return f to s
}

infix fun <S, E> S.toSetOf(that: E?): Pair<S, Set<E>> =
    Pair(this, if (that != null) setOf(that) else emptySet())

infix fun <S, E> Pair<S, Set<E>>.plus(that: E?): Pair<S, Set<E>> =
    Pair(first, second + if (that != null) setOf(that) else emptySet())

fun <A, B> A.withEmptySet(): Pair<A, Set<B>> = Pair(this, setOf())

inline fun <reified T : Enum<T>> T.nextEnumValue(): T =
    enumValues<T>().run {
        this[(ordinal + 1) % this.count()]
    }