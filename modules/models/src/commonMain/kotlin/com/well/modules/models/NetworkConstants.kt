package com.well.modules.models

class NetworkConstants private constructor(private val isDebug: Boolean) {
    enum class URLProtocol {
        HTTP,
        HTTPS,
        WS,
        WSS,
        ;
    }

    fun oauthCallbackPath(path: String = "") = NetworkConstants.oauthCallbackPath(path)

    val localHttpProtocol = if (isDebug) URLProtocol.HTTP else URLProtocol.HTTPS
    val localWebSocketProtocol = if (localHttpProtocol == URLProtocol.HTTPS) URLProtocol.WSS else URLProtocol.WS
    val host = if (isDebug)
        "dukhovwellserver-new.com"
    else
        "well-env-1.eba-yyqqrxsi.us-east-2.elasticbeanstalk.com"
    val port = if (isDebug) 8090 else null

    override fun toString() = "isDebug: $isDebug; localHttpProtocol: $localHttpProtocol; localWebSocketProtocol: $localWebSocketProtocol; host: $host"

    companion object {
        val debug = NetworkConstants(true)
        val release = NetworkConstants(false)

        val base = release

        fun current(isDebug: Boolean) = if (isDebug) debug else release

        val oauthCallbackProtocol = "world.endo.live.link"
        fun oauthCallbackPath(path: String = "") = "$oauthCallbackProtocol://$path"
    }
}