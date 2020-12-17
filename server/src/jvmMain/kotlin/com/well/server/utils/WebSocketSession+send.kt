package com.well.server.utils

import com.well.serverModels.WebSocketMessage
import io.ktor.http.cio.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

suspend fun WebSocketSession.send(message: WebSocketMessage) =
    send(
        Frame.Text(
        Json.encodeToString(message)
    ))