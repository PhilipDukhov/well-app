package com.well.utils

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class SyncFeature<Msg : Any, Model : Any, Eff : Any>(
    initialState: Model,
    initialEffects: Set<Eff>,
    private val reducer: (Msg, Model) -> Pair<Model, Set<Eff>>,
    override val coroutineContext: CoroutineContext
) : Feature<Msg, Model, Eff> {
    override var currentState: Model = initialState
        private set

    private val stateListeners = SafeListenersList(mutableListOf<(state: Model) -> Unit>())
    private val effListeners = SafeListenersList(mutableListOf<(eff: Eff) -> Unit>())

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

    override fun listenState(listener: (state: Model) -> Unit): Closeable =
        CloseableFuture(this) {
            val closeable = stateListeners.addListenerAndMakeCloseable(listener)
            MainScope().launch {
                listener(currentState)
            }
            closeable
        }

    override fun listenEffect(listener: (eff: Eff) -> Unit): Closeable =
        CloseableFuture(this) {
            effListeners.addListenerAndMakeCloseable(listener)
        }
}