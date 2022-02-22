package com.well.modules.puerhBase

import com.well.modules.atomic.AtomicMutableList
import com.well.modules.atomic.AtomicRef
import com.well.modules.atomic.Closeable
import com.well.modules.atomic.CloseableContainer
import com.well.modules.atomic.asCloseable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

abstract class EffectHandler<Eff : Any, Msg : Any>(coroutineScope: CoroutineScope) : CloseableContainer() {
    val effHandlerScope: CoroutineScope
    private var listener by AtomicRef<((Msg) -> Unit)?>(null)
    private val pendingMessages = AtomicMutableList<Msg>()

    init {
        val job = Job()
        this.effHandlerScope = coroutineScope + job
        addCloseableChild(job.asCloseable())
    }

    open fun setListener(listener: suspend (Msg) -> Unit) {
        this.listener = { msg -> effHandlerScope.launch { listener(msg) } }
        pendingMessages
            .dropAll()
            .forEach(::listener)
    }

    fun handleEffect(eff: Eff) {
        effHandlerScope.launch {
            processEffect(eff)
        }
    }

    protected abstract suspend fun processEffect(eff: Eff)

    open fun listener(msg: Msg) {
        listener?.invoke(msg)
            ?: run {
                pendingMessages.add(msg)
            }
    }
}

fun <Eff1 : Any, Msg1 : Any, Eff2 : Any, Msg2 : Any> EffectHandler<Eff1, Msg1>.adapt(
    effAdapter: (Eff2) -> Eff1?,
    msgAdapter: (Msg1) -> Msg2?,
): EffectHandler<Eff2, Msg2> = object : EffectHandler<Eff2, Msg2>(effHandlerScope) {
    init {
        addCloseableChild(this@adapt)
    }

    override fun setListener(listener: suspend (Msg2) -> Unit) =
        setListener { msg: Msg1 -> msgAdapter(msg)?.let { listener(it) } }

    override suspend fun processEffect(eff: Eff2) {
        effAdapter(eff)?.let { handleEffect(it) }
    }
}

fun <Msg : Any, State : Any, Eff : Any> Feature<Msg, State, Eff>.wrapWithEffectHandler(
    effectHandler: EffectHandler<Eff, Msg>,
    initialEffects: Set<Eff> = emptySet(),
) = run {
    addEffectHandler(effectHandler, initialEffects)
}

fun <Msg : Any, State : Any, Eff : Any> Feature<Msg, State, Eff>.addEffectHandler(
    effectHandler: EffectHandler<Eff, Msg>,
    initialEffects: Set<Eff> = emptySet(),
): Closeable {
    effectHandler.setListener { msg -> accept(msg) }
    val closeable = listenEffect { eff ->
        effectHandler.handleEffect(eff)
    }
    effectHandler.addCloseableChild(closeable)
    initialEffects.forEach(effectHandler::handleEffect)
    return effectHandler
}