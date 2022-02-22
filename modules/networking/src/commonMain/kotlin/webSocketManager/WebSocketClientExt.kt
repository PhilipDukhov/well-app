package com.well.modules.networking.webSocketManager

internal expect suspend fun WebSocketClient.ws(
    path: String,
    block: suspend WebSocketSession.() -> Unit,
)