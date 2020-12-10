package com.well.utils

import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

private typealias MsgListener<Msg> = suspend (Msg) -> Unit
typealias SyncEffectInterpreter<Eff, Msg> = suspend (MsgListener<Msg>).(Eff) -> Unit

class SyncEffectHandler<Eff : Any, Msg : Any>(
    private val effectInterpreter: SyncEffectInterpreter<Eff, Msg>,
    override val coroutineContext: CoroutineContext
) : EffectHandler<Eff, Msg> {
    private var listener: MsgListener<Msg>? = null

    override fun setListener(listener: MsgListener<Msg>) {
        this.listener = listener
    }

    override fun handleEffect(eff: Eff) {
        val listener = listener ?: {}
        launch(coroutineContext) {
            effectInterpreter.invoke(listener, eff)
        }
    }
}