package com.well.shared.networking

import kotlinx.coroutines.channels.ReceiveChannel

expect class WebSocketSession {
    val incoming: ReceiveChannel<String>
    suspend fun send(text: String)
    suspend fun send(data: ByteArray)
}