package com.well.modules.networking

import com.well.modules.models.NetworkConstants
import io.ktor.http.*

val NetworkConstants.httpProtocol get() = localHttpProtocol.toUrlProtocol()
val NetworkConstants.webSocketProtocol get() = localWebSocketProtocol.toUrlProtocol()

private fun NetworkConstants.URLProtocol.toUrlProtocol() = when (this) {
    NetworkConstants.URLProtocol.HTTP -> URLProtocol.HTTP
    NetworkConstants.URLProtocol.HTTPS -> URLProtocol.HTTPS
    NetworkConstants.URLProtocol.WS -> URLProtocol.WS
    NetworkConstants.URLProtocol.WSS -> URLProtocol.WSS
}
