package com.well.sharedMobile.networking.webSocketManager

import com.well.sharedMobile.networking.createBaseServerClient
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.websocket.*
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
                contentType(ContentType.Application.Json)
            }
            install(WebSockets)
        }
    }
}

