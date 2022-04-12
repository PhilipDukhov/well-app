package com.well.modules.networking.webSocketManager

import com.well.modules.networking.createBaseServerClient
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*

internal class WebSocketClient(val config: Config) {
    data class Config(
        val host: String,
        val port: Int?,
        val webSocketProtocol: URLProtocol,
        val bearerToken: String,
    ) {
        val header: Pair<String, String>
            get() = HttpHeaders.Authorization to "Bearer $bearerToken"
    }

    val client: HttpClient by lazy {
        createBaseServerClient().config {
            defaultRequest {
                config.header.apply {
                    header(first, second)
                }
            }
            install(WebSockets) {
                pingInterval = 5_000
            }
            install(Logging) {
                level = LogLevel.ALL
            }
        }
    }
}