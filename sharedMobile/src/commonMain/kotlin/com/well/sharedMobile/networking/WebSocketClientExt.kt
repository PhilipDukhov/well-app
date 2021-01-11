package com.well.sharedMobile.networking

internal expect suspend fun WebSocketClient.ws(
    path: String,
    block: suspend WebSocketSession.() -> Unit,
)

internal expect suspend fun WebSocketClient.wss(
    path: String,
    block: suspend WebSocketSession.() -> Unit,
)