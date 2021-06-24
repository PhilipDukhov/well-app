package com.well.modules.utils.puerh

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.launch

typealias ExecutorEffectsInterpreter<Eff, Msg> = suspend CoroutineScope.(eff: Eff, listener: (Msg) -> Unit) -> Unit

class ExecutorEffectHandler<Msg : Any, Eff : Any>(
    private val effectsInterpreter: ExecutorEffectsInterpreter<Eff, Msg>,
    coroutineScope: CoroutineScope,
) : EffectHandler<Eff, Msg>(coroutineScope) {

    override fun handleEffect(eff: Eff) {
        coroutineScope.launch {
            effectsInterpreter(eff, listener ?: {})
        }
    }
}
