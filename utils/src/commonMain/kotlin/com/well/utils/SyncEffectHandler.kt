package com.well.utils

//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.MainScope
//import kotlinx.coroutines.launch

//private typealias MsgListener<Msg> = (Msg) -> Unit
//typealias SyncEffectInterpreter<Eff, Msg> = suspend (MsgListener<Msg>).(Eff) -> Unit
//
//class SyncEffectHandler<Eff : Any, Msg : Any>(
//    private val effectInterpreter: SyncEffectInterpreter<Eff, Msg>,
//    override val coroutineScope: CoroutineScope
//) : EffectHandler<Eff, Msg>(coroutineScope) {
//    override fun handleEffect(eff: Eff) {
//        val listener = listener ?: {}
//
//        coroutineScope.launch {
//            effectInterpreter.invoke(listener, eff)
//        }
//    }
//}