package com.well.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

expect fun <T> T.freeze(): T

inline fun <A, B, RA, RB> Pair<A, B>.map(transformA: (A) -> RA, transformB: (B) -> RB) =
    transformA(first) to transformB(second)