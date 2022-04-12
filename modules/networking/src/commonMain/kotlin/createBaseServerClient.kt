package com.well.modules.networking

import com.well.modules.models.NetworkConstants
import com.well.modules.utils.ktorUtils.createBaseHttpClient
import com.well.modules.utils.viewUtils.platform.Platform
import com.well.modules.utils.viewUtils.platform.isLocalServer
import io.ktor.client.plugins.*
import io.ktor.http.*

fun createBaseServerClient() = createBaseHttpClient().config {
    defaultRequest {
        NetworkConstants.current(Platform.isLocalServer).let { constants ->
            if (url.protocol == URLProtocol.HTTP) {
                url.protocol = constants.httpProtocol
            }
            host = constants.host
            constants.port?.let(::port::set)
        }
    }
}