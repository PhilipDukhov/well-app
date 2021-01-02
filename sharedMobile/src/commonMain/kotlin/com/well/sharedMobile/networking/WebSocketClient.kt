package com.well.sharedMobile.networking

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

internal class WebSocketClient(val config: Config) {
    data class Config(
        val host: String,
        val port: Int,
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
            install(WebSockets)
        }
    }
}

