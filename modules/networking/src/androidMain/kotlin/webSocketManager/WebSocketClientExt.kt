package com.well.modules.networking.webSocketManager

import io.ktor.client.features.websocket.*
import kotlinx.coroutines.coroutineScope

internal actual suspend fun WebSocketClient.ws(
    path: String,
    block: suspend WebSocketSession.() -> Unit,
) {
    client.ws(path, request = {
        url.protocol = config.webSocketProtocol
    }) {
        try {
            val session = this
            coroutineScope {
                block(WebSocketSession(session, this))
            }
        } catch (t: Throwable) {
            throw t
        }
    }
}