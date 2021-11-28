package com.well.modules.puerhBase

import com.well.modules.atomic.Closeable

interface Feature<Msg : Any, State : Any, Eff : Any> : FeatureProvider<Msg, State> {
    fun listenEffect(listener: (eff: Eff) -> Unit): Closeable
}