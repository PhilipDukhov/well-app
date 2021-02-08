package com.well.sharedMobile.networking

import com.well.serverModels.createBaseHttpClient
import com.well.utils.platform.Platform
import com.well.utils.platform.isDebug
import io.ktor.client.features.*
import io.ktor.client.request.*

fun createBaseServerClient() = createBaseHttpClient().config {
    defaultRequest {
        host = Constants.host
        port = Constants.port
    }
}

object Constants {
    val host = if (Platform.isDebug)
        "dukhovwellserver.com"
    else
        "well-env-1.eba-yyqqrxsi.us-east-2.elasticbeanstalk.com"
    val port = if (Platform.isDebug) 8090 else 80
}