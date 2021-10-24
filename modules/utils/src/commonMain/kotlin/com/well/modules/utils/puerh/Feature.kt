package com.well.modules.utils.puerh

import com.well.modules.atomic.Closeable

interface Feature<Msg : Any, State : Any, Eff : Any> : FeatureProvider<Msg, State> {
    val currentState: State
    fun listenEffect(listener: (eff: Eff) -> Unit): Closeable
}
