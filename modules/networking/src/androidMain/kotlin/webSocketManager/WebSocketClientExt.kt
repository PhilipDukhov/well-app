package com.well.modules.networking.webSocketManager

import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import kotlinx.coroutines.coroutineScope

internal actual suspend fun WebSocketClient.ws(
    path: String,
    block: suspend WebSocketSession.() -> Unit,
) {
    client.ws(path, request = {
        url.protocol = URLProtocol.WS
    }) {
        val session = this
        coroutineScope {
            block(WebSocketSession(session, this))
        }
    }
}