package com.well.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import com.github.aakira.napier.Napier
import com.well.utils.atomic.AtomicRef

abstract class EffectHandler<Eff : Any, Msg : Any>(open val coroutineScope: CoroutineScope) : CloseableContainer() {
    private val _listener = AtomicRef<((Msg) -> Unit)?>(null)
    var listener: ((Msg) -> Unit)?
        get() = _listener.value
        set(value) { _listener.value = value }

    open fun setListener(listener: suspend (Msg) -> Unit) {
        this.listener = { msg -> coroutineScope.launch { listener(msg) } }
    }

    abstract fun handleEffect(eff: Eff)
}

fun <Eff1 : Any, Msg1 : Any, Eff2 : Any, Msg2 : Any> EffectHandler<Eff1, Msg1>.adapt(
    effAdapter: (Eff2) -> Eff1?,
    msgAdapter: (Msg1) -> Msg2?
): EffectHandler<Eff2, Msg2> = object : EffectHandler<Eff2, Msg2>(coroutineScope) {
    override fun setListener(listener: suspend (Msg2) -> Unit) =
        setListener { msg: Msg1 -> msgAdapter(msg)?.let { listener(it) } }

    override fun handleEffect(eff: Eff2) {
        effAdapter(eff)?.let { handleEffect(it) }
    }
}

fun <Msg : Any, State : Any, Eff : Any> Feature<Msg, State, Eff>.wrapWithEffectHandler(
    effectHandler: EffectHandler<Eff, Msg>,
    initialEffects: Set<Eff> = emptySet()
) = apply {
    addEffectHandler(effectHandler, initialEffects)
}

fun <Msg : Any, State : Any, Eff : Any> Feature<Msg, State, Eff>.addEffectHandler(
    effectHandler: EffectHandler<Eff, Msg>,
    initialEffects: Set<Eff> = emptySet()
): Closeable {
    effectHandler.setListener { msg -> accept(msg) }
    val closeable = listenEffect { eff ->
        effectHandler.handleEffect(eff)
    }
    effectHandler.addCloseableChild(closeable)
    initialEffects.forEach(effectHandler::handleEffect)
    return closeable
}
