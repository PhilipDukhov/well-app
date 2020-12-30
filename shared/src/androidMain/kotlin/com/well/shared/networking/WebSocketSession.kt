package com.well.shared.networking

import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlin.coroutines.CoroutineContext

actual class WebSocketSession(
    private val socketSession: DefaultClientWebSocketSession,
) {
    private val channel = Channel<String>(8)
    actual val incoming: ReceiveChannel<String> = channel

    init {
        GlobalScope.launch(Dispatchers.IO) {
            for (frame in socketSession.incoming) {
                when (frame) {
                    is Frame.Text -> channel.send(frame.readText())
                    else -> channel.send(frame.toString())
                }
            }
        }

    }

    actual suspend fun send(text: String) =
        socketSession.send(text)

    actual suspend fun send(data: ByteArray) =
        socketSession.send(data)
}