package com.well.utils

expect fun <T> T.freeze(): T

inline fun <A, B, RA, RB> Pair<A, B>.map(transformA: (A) -> RA, transformB: (B) -> RB) =
    transformA(first) to transformB(second)