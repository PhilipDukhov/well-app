package com.well.modules.puerhBase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
    }

    protected abstract fun handleEffs(effs: Set<Eff>)
}