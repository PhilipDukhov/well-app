package com.well.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.coroutineContext

class SafeListenersList<T, E: (T) -> Unit>(
    private val list: MutableList<E>
) {
    private val mutex = Mutex()

    private suspend fun access(handler: MutableList<E>.() -> Unit) =
        mutex.withLock {
            handler(list)
        }

    suspend fun notifyAll(msg: T) = access { forEach { listener -> listener.invoke(msg) } }

    suspend fun addListenerAndMakeCloseable(listener: E): Closeable =
        listener.run {
            access {
                add(this@run)
            }
            val context = coroutineContext
            object : Closeable {
                override fun close() {
                    CoroutineScope(context).launch {
                        access {
                            remove(this@run)
                        }
                    }
                }
            }
        }
}