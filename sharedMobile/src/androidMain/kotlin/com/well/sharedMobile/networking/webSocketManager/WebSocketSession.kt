package com.well.sharedMobile.networking.webSocketManager

import com.well.modules.napier.Napier
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlin.coroutines.CoroutineContext

actual class WebSocketSession(
    private val socketSession: DefaultClientWebSocketSession,
    scope: CoroutineScope,
) {
    private val channel = Channel<String>(8)
    actual val incoming: ReceiveChannel<String> = channel

    init {
        scope.launch {
            for (frame in socketSession.incoming) {
                when (frame) {
                    is Frame.Text -> {
                        Napier.i("WebSocketSession receive ${frame.readText()}")
                        channel.send(frame.readText())
                    }
                    else -> {
                        Napier.i("WebSocketSession receive not text $frame}")
                        channel.send(frame.toString())
                    }
                }
            }
        }

    }

    actual suspend fun send(text: String) {
        Napier.i("WebSocketSession send $text")
        socketSession.send(text)
    }

    actual suspend fun send(data: ByteArray) =
        socketSession.send(data)
}