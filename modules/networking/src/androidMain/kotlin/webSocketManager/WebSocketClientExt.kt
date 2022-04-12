package com.well.modules.networking.webSocketManager

import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import kotlinx.coroutines.coroutineScope

internal actual suspend fun WebSocketClient.ws(
    path: String,
    block: suspend WebSocketSession.() -> Unit,
) {
    client.ws(path, request = {
//        url.protocol = config.webSocketProtocol
        url.protocol = URLProtocol.WS
    }) {
//    client.ws(
//        urlString = "ws://dukhovwellserver-new.com:8090/$path",
//    ) {
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