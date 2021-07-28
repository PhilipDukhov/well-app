package com.well.sharedMobile.networking.webSocketManager

import io.ktor.client.features.websocket.*
import kotlinx.coroutines.CoroutineScope

internal actual suspend fun WebSocketClient.ws(
    path: String,
    block: suspend WebSocketSession.() -> Unit,
) {
    client.ws(path, request = {
        url.protocol = config.webSocketProtocol
    }) {
        println("WebSocketClient start")
        try {
            block(WebSocketSession(this, CoroutineScope(coroutineContext)))
        } catch (t: Throwable) {
            println("WebSocketClient crash $t")
            throw t
        }
        println("WebSocketClient end")
    }
}