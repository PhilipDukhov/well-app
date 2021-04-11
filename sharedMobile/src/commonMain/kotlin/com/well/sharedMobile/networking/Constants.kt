package com.well.sharedMobile.networking

import com.well.modules.models.createBaseHttpClient
import com.well.modules.utils.platform.Platform
import com.well.modules.utils.platform.isDebug
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