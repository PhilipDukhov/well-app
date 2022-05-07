package com.well.modules.networking.webSocketManager

import com.well.modules.networking.createBaseServerClient
import com.well.modules.utils.ktorUtils.UnauthorizedException
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*

internal class WebSocketClient(val config: Config, val onUnauthorized: () -> Unit) {
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
            HttpResponseValidator {
                handleResponseException { cause ->
                    if (cause is UnauthorizedException) {
                        onUnauthorized()
                    }
                }
            }
        }
    }
}