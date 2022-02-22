package com.well.modules.puerhBase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias ExecutorEffectsInterpreter<Eff, Msg> = suspend CoroutineScope.(eff: Eff, listener: kotlin.reflect.KFunction1<Msg, Unit>) -> Unit

class ExecutorEffectHandler<Msg : Any, Eff : Any>(
    private val effectsInterpreter: ExecutorEffectsInterpreter<Eff, Msg>,
    parentCoroutineScope: CoroutineScope,
) : EffectHandler<Eff, Msg>(parentCoroutineScope) {

    override suspend fun processEffect(eff: Eff) {
        effHandlerScope.launch {
            effectsInterpreter(eff, ::listenerWrapper)
        }
    }

    private fun listenerWrapper(msg: Msg) {
        listener(msg)
    }
}