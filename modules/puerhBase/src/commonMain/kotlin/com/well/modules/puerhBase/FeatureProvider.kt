package com.well.modules.puerhBase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface FeatureProvider<Msg : Any, State : Any> : CoroutineScope {
    fun accept(msg: Msg)
    val state: StateFlow<State>
}