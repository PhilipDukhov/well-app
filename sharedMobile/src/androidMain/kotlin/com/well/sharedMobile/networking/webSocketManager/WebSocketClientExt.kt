package com.well.sharedMobile.networking.webSocketManager

import io.ktor.client.features.websocket.*

internal actual suspend fun WebSocketClient.ws(
    path: String,
    block: suspend WebSocketSession.() -> Unit,
) {
    client.ws(path, request = {
        url.protocol = config.webSocketProtocol
    }) {
        block(WebSocketSession(this))
    }
}