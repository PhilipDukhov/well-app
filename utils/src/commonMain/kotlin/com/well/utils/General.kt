package com.well.utils

expect fun <T> T.freeze(): T

inline fun <A, B, RA, RB> Pair<A, B>.map(transformA: (A) -> RA, transformB: (B) -> RB) =
    transformA(first) to transformB(second)

infix fun <A, B> A.toSetOf(that: B?): Pair<A, Set<B>> = Pair(this, if (that != null) setOf(that) else emptySet())
fun <A, B> A.withEmptySet(): Pair<A, Set<B>> = Pair(this, setOf())