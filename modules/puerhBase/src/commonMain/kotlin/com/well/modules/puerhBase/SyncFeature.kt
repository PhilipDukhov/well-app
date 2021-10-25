package com.well.modules.puerhBase

import com.well.modules.atomic.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.CoroutineContext

class SyncFeature<Msg : Any, Model : Any, Eff : Any>(
    initialState: Model,
    initialEffects: Set<Eff>,
    private val reducer: (Msg, Model) -> Pair<Model, Set<Eff>>,
    override val coroutineContext: CoroutineContext
) : Feature<Msg, Model, Eff> {
    private val effectScope = CoroutineScope(Dispatchers.Default)
    private val reducerMutex = Mutex()
    override var currentState by AtomicRef(initialState)
        private set

    private val stateListeners = AtomicMutableList<(state: Model) -> Unit>()
    private val effListeners = AtomicMutableList<(eff: Eff) -> Unit>()

    init {
        effectScope.launch {
            delay(100)
            initialEffects.forEach { command ->
                effListeners.notifyAll(command)
            }
        }
    }

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