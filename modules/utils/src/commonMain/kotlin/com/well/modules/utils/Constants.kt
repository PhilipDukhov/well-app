package com.well.modules.utils

import com.well.modules.models.BaseConstants
import com.well.modules.utils.platform.Platform
import com.well.modules.utils.platform.isDebug
import io.ktor.http.*

object Constants: BaseConstants() {
    val httpProtocol = if (Platform.isDebug) URLProtocol.HTTP else URLProtocol.HTTPS
    val webSocketProtocol = if (httpProtocol == URLProtocol.HTTPS) URLProtocol.WSS else URLProtocol.WS
    val host = if (Platform.isDebug)
        "dukhovwellserver-new.com"
    else
        "well-env-1.eba-yyqqrxsi.us-east-2.elasticbeanstalk.com"
    val port = if (Platform.isDebug) 8090 else null
}