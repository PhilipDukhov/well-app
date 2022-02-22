package com.well.modules.puerhBase

infix fun <S, E> S.toSetOf(that: E?): Pair<S, Set<E>> =
    Pair(this, if (that != null) setOf(that) else emptySet())

fun <A, B> A.withEmptySet(): Pair<A, Set<B>> = Pair(this, setOf())

infix fun <S, E> Pair<S, Set<E>>.plus(that: E?): Pair<S, Set<E>> =
    Pair(first, second + if (that != null) setOf(that) else emptySet())