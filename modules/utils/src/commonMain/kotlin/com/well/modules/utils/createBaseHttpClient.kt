package com.well.modules.utils

import com.well.modules.models.createBaseHttpClient
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