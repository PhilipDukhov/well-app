package com.well.modules.utils.puerh

import com.well.modules.atomic.Closeable
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class ReducerViewModel<State, Msg, Eff>(
    initial: State,
    private val reducer: (Msg, State) -> Pair<State, Set<Eff>>,
) {
    private val _state = MutableStateFlow(initial)
    val state: StateFlow<State> = _state

    fun listener(msg: Msg) {
        val (newState, effs) = reducer(msg, _state.value)
        _state.value = newState
        handleEffs(effs)
        Napier.d("$msg $newState")
    }

    protected abstract fun handleEffs(effs: Set<Eff>)
}