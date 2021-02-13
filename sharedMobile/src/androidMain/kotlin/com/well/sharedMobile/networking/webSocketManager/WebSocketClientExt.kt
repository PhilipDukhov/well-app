package com.well.sharedMobile.networking.webSocketManager

import io.ktor.client.features.websocket.*

internal actual suspend fun WebSocketClient.ws(
    path: String,
    block: suspend WebSocketSession.() -> Unit,
) {
    client.ws(path) {
        block(WebSocketSession(this))
    }
}

internal actual suspend fun WebSocketClient.wss(
    path: String,
    block: suspend WebSocketSession.() -> Unit,
) {
    client.wss(path) {
        block(WebSocketSession(this))
    }
}