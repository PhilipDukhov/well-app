package com.well.utils

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.isActive
import kotlin.coroutines.CoroutineContext

class SyncFeature<Msg : Any, Model : Any, Eff : Any>(
    initialState: Model,
    private val reducer: (Msg, Model) -> Pair<Model, Set<Eff>>,
    override val coroutineContext: CoroutineContext
) : Feature<Msg, Model, Eff> {
    override var currentState: Model = initialState
        private set

    private val stateListeners = mutableListOf<(state: Model) -> Unit>()
    private val effListeners = mutableListOf<(eff: Eff) -> Unit>()

    @InternalCoroutinesApi
    override fun accept(msg: Msg) {
        if (!NonCancellable.isActive) return
        val (newState, commands) = reducer(msg, currentState)
        currentState = newState
        stateListeners.notifyAll(newState)
        commands.forEach { command ->
            effListeners.notifyAll(command)
        }
    }

    override fun listenState(listener: (state: Model) -> Unit): Closeable {
        val closeable = stateListeners.addListenerAndMakeCancelable(listener)
        listener(currentState)
        return closeable
    }

    override fun listenEffect(listener: (eff: Eff) -> Unit): Closeable =
        effListeners.addListenerAndMakeCancelable(listener)
}
