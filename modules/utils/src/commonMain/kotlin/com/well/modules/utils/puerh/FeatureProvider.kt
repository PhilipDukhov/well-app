package com.well.modules.utils.puerh

import com.well.modules.atomic.Closeable
import kotlinx.coroutines.CoroutineScope

interface FeatureProvider<Msg : Any, State : Any> : CoroutineScope {
    fun accept(msg: Msg)
    fun listenState(listener: (model: State) -> Unit): Closeable
}