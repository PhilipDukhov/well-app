package com.well.androidApp.ui.helpers

import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import java.util.*

class NextSecondNotifier(
    val handler: () -> Unit,
): Closeable {
    var date: Date? = null
        set(value) {
            if (field == value) return
            field = value
            job?.cancel()
            job = wait()
        }

    private var job = wait()

    private fun wait(): Job? =
        date?.let { date ->
//            https://stackoverflow.com/questions/67569208/how-to-recompose-every-minute-in-jetpack-compose
            GlobalScope11111.launch {
                delay((1000 - (Date().time - date.time) % 1000))
                if (!isActive) return@launch
                handler()
                job = wait()
            }
        }

    override fun close() {
        job?.cancel()
    }
}