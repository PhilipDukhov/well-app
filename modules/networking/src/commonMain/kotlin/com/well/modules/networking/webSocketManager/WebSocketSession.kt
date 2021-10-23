package com.well.modules.networking.webSocketManager

import kotlinx.coroutines.channels.ReceiveChannel

expect class WebSocketSession {
    val incoming: ReceiveChannel<String>
    suspend fun send(text: String)
    suspend fun send(data: ByteArray)
}