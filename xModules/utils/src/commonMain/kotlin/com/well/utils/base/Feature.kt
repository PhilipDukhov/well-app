package com.well.utils

import kotlinx.coroutines.CoroutineScope
import com.well.atomic.Closeable

interface Feature<Msg : Any, Model : Any, Eff : Any> : CoroutineScope {
    val currentState: Model
    fun accept(msg: Msg)
    fun listenState(listener: (model: Model) -> Unit): Closeable
    fun listenEffect(listener: (eff: Eff) -> Unit): Closeable
}