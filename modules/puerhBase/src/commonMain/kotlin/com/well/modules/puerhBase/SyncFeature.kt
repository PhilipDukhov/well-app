package com.well.modules.puerhBase

import com.well.modules.atomic.AtomicMutableList
import com.well.modules.atomic.Closeable
import com.well.modules.atomic.addListenerAndMakeCloseable
import com.well.modules.atomic.notifyAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.CoroutineContext

class SyncFeature<Msg : Any, State : Any, Eff : Any>(
    initialState: State,
    private val reducer: (Msg, State) -> Pair<State, Set<Eff>>,
    override val coroutineContext: CoroutineContext,
) : Feature<Msg, State, Eff> {
    private val effectScope = CoroutineScope(Dispatchers.Default)
    private val reducerMutex = Mutex()

    override val state: MutableStateFlow<State> = MutableStateFlow(initialState)

    private val effListeners = AtomicMutableList<(eff: Eff) -> Unit>()

    override fun accept(msg: Msg) {
        launch(coroutineContext) {
            if (!isActive) return@launch
            reducerMutex.lock()
            val (newState, commands) = reducer(msg, state.value)
            state.value = newState
            reducerMutex.unlock()
            if (commands.isNotEmpty()) {
                effectScope.launch {
                    commands.forEach { command ->
                        effListeners.notifyAll(command)
                    }
                }
            }
        }
    }

    override fun listenEffect(listener: (eff: Eff) -> Unit): Closeable =
        effListeners.addListenerAndMakeCloseable(listener)
}