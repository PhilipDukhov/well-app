package com.well.sharedMobile.networking

import com.well.modules.models.createBaseHttpClient
import com.well.modules.utils.platform.Platform
import com.well.modules.utils.platform.isDebug
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*

fun createBaseServerClient() = createBaseHttpClient().config {
    defaultRequest {
        url.protocol = Constants.protocol
        host = Constants.host
        if (Constants.port != null) {
            port = Constants.port
        }
    }
}

object Constants: com.well.modules.models.BaseConstants() {
    val protocol = if (Platform.isDebug) URLProtocol.HTTP else URLProtocol.HTTPS
    val webSocketProtocol = if (protocol == URLProtocol.HTTPS) URLProtocol.WSS else URLProtocol.WS
    val host = if (Platform.isDebug)
        "dukhovwellserver.com"
    else
        "well-env-1.eba-yyqqrxsi.us-east-2.elasticbeanstalk.com"
    val port = if (Platform.isDebug) 8090 else null
}