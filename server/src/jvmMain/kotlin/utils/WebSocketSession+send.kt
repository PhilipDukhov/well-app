package com.well.server.utils

import com.well.modules.models.WebSocketMsg
import io.ktor.http.cio.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

suspend fun WebSocketSession.send(msg: WebSocketMsg.Call) = send(msg as WebSocketMsg)

suspend fun WebSocketSession.send(msg: WebSocketMsg.Back) = send(msg as WebSocketMsg)

private suspend fun WebSocketSession.send(msg: WebSocketMsg) =
    send(
        Frame.Text(
            Json.encodeToString(msg)
        )
    )