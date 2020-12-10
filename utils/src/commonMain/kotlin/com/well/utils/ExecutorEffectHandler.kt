package com.well.utils

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.launch

typealias ExecutorEffectsInterpreter<Eff, Msg> = suspend CoroutineScope.(eff: Eff, listener: (Msg) -> Unit) -> Unit

class ExecutorEffectHandler<Msg : Any, Eff : Any>(
    private val effectsInterpreter: ExecutorEffectsInterpreter<Eff, Msg>,
    override val coroutineContext: CoroutineContext,
) : EffectHandler<Eff, Msg> {
    private var listener: ((Msg) -> Unit)? = null

    override fun setListener(listener: suspend (Msg) -> Unit) {
        this.listener = { msg ->
            launch(coroutineContext) { listener(msg) }
        }
    }

    override fun handleEffect(eff: Eff) {
        launch(coroutineContext) {
            effectsInterpreter(eff, listener ?: {})
        }
    }
}
