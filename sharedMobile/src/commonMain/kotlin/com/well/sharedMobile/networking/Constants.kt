package com.well.sharedMobile.networking

import com.well.serverModels.createBaseHttpClient
import io.ktor.client.features.*
import io.ktor.client.request.*

fun createBaseServerClient() = createBaseHttpClient().config {
    defaultRequest {
        host = Constants.host
        port = Constants.port
    }
}

object Constants {
    const val host = "dukhovwellserver.com"
    const val port = 8090
}