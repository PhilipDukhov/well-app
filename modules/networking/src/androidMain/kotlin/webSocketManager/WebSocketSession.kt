package com.well.modules.networking.webSocketManager

import io.github.aakira.napier.Napier
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

actual class WebSocketSession(
    private val socketSession: DefaultClientWebSocketSession,
    private val scope: CoroutineScope,
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
        try {
            socketSession.send(text)
        } catch (e: Exception) {
            socketSession.cancel("send failed", e)
            scope.cancel("send failed", e)
        }
    }
}