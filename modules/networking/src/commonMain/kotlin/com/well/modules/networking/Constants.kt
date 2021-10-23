package com.well.modules.networking

import com.well.modules.models.createBaseHttpClient
import com.well.modules.utils.platform.Platform
import com.well.modules.utils.platform.isDebug
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*

fun createBaseServerClient() = createBaseHttpClient().config {
    defaultRequest {
        if (url.protocol == URLProtocol.HTTP) {
            url.protocol = Constants.httpProtocol
        }
        host = Constants.host
        if (Constants.port != null) {
            port = Constants.port
        }
    }
}

object Constants: com.well.modules.models.BaseConstants() {
    val httpProtocol = if (Platform.isDebug) URLProtocol.HTTP else URLProtocol.HTTPS
    val webSocketProtocol = if (httpProtocol == URLProtocol.HTTPS) URLProtocol.WSS else URLProtocol.WS
    val host = if (Platform.isDebug)
        "dukhovwellserver-new.com"
    else
        "well-env-1.eba-yyqqrxsi.us-east-2.elasticbeanstalk.com"
    val port = if (Platform.isDebug) 8090 else null
}