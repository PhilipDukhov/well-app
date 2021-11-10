package com.well.modules.puerhBase

import com.well.modules.atomic.AtomicMutableList
import com.well.modules.atomic.AtomicRef
import com.well.modules.atomic.Closeable
import com.well.modules.atomic.addListenerAndMakeCloseable
import com.well.modules.atomic.notifyAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.CoroutineContext

class SyncFeature<Msg : Any, Model : Any, Eff : Any>(
    initialState: Model,
    private val reducer: (Msg, Model) -> Pair<Model, Set<Eff>>,
    override val coroutineContext: CoroutineContext,
) : Feature<Msg, Model, Eff> {
    private val effectScope = CoroutineScope(Dispatchers.Default)
    private val reducerMutex = Mutex()
    override var currentState by AtomicRef(initialState)
        private set

    private val stateListeners = AtomicMutableList<(state: Model) -> Unit>()
    private val effListeners = AtomicMutableList<(eff: Eff) -> Unit>()

    override fun accept(msg: Msg) {
        launch(coroutineContext) {
            reducerMutex.lock()
            if (!isActive) return@launch
            val (newState, commands) = reducer(msg, currentState)
            currentState = newState
            reducerMutex.unlock()
            MainScope().launch {
                stateListeners.notifyAll(newState)
            }
            if (commands.isNotEmpty()) {
                effectScope.launch {
                    commands.forEach { command ->
                        effListeners.notifyAll(command)
                    }
                }
            }
        }
    }

    override fun listenState(listener: (state: Model) -> Unit): Closeable {
        val closeable = stateListeners.addListenerAndMakeCloseable(listener)
        MainScope().launch {
            listener(currentState)
        }
        return closeable
    }

    override fun listenEffect(listener: (eff: Eff) -> Unit): Closeable =
        effListeners.addListenerAndMakeCloseable(listener)
}