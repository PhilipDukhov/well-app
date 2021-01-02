package com.well.utils

import com.well.utils.atomic.AtomicMutableList
import com.well.utils.atomic.AtomicRef
import com.well.utils.atomic.addListenerAndMakeCloseable
import com.well.utils.atomic.notifyAll
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class SyncFeature<Msg : Any, Model : Any, Eff : Any>(
    initialState: Model,
    initialEffects: Set<Eff>,
    private val reducer: (Msg, Model) -> Pair<Model, Set<Eff>>,
    override val coroutineContext: CoroutineContext
) : Feature<Msg, Model, Eff> {
    private val _currentState = AtomicRef(initialState)
    override var currentState: Model
        get() = _currentState.value
        private set(value) {
            _currentState.value = value
        }

    private val stateListeners = AtomicMutableList<(state: Model) -> Unit>()
    private val effListeners = AtomicMutableList<(eff: Eff) -> Unit>()

    init {
        launch(coroutineContext) {
            delay(100)
            initialEffects.forEach { command ->
                effListeners.notifyAll(command)
            }
        }
    }

    @InternalCoroutinesApi
    override fun accept(msg: Msg) {
        if (!NonCancellable.isActive) return
        val (newState, commands) = reducer(msg, currentState)
        currentState = newState
        MainScope().launch {
            stateListeners.notifyAll(newState)
        }
        launch(coroutineContext) {
            commands.forEach { command ->
                effListeners.notifyAll(command)
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