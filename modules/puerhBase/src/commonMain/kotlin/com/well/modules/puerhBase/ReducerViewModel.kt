package com.well.modules.puerhBase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

abstract class ReducerViewModel<State, Msg, Eff>(
    initial: State,
    private val reducer: (Msg, State) -> Pair<State, Set<Eff>>,
) {
    private val _state = MutableStateFlow(initial)
    val state = _state.asStateFlow()

    private val reducerMutex = Mutex()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    open fun listener(msg: Msg) {
        coroutineScope.launch {
            reducerMutex.lock()
            val (newState, effs) = reducer(msg, _state.value)
            _state.value = newState
            reducerMutex.unlock()
            handleEffs(effs)
        }
    }

    protected abstract fun handleEffs(effs: Set<Eff>)
}