package com.well.modules.networking

import com.well.modules.utils.ktorUtils.UnauthorizedException
import io.ktor.http.*

internal fun Exception.toResponseException(): Exception =
    when (this) {
        is io.ktor.client.plugins.RedirectResponseException -> RedirectResponseException(this)
        is io.ktor.client.plugins.ClientRequestException -> ClientRequestException(this)
        is io.ktor.client.plugins.ServerResponseException -> ServerResponseException(this)
        else -> this
    }

internal fun getException(httpStatusCode: Int): Exception? {
    val code = HttpStatusCode.fromValue(httpStatusCode)
    return when (httpStatusCode) {
        in 200 until 300 -> null
        HttpStatusCode.Unauthorized.value -> UnauthorizedException()
        in 300..399 -> RedirectResponseException(code)
        in 400..499 -> ClientRequestException(code)
        in 500..599 -> ServerResponseException(code)
        else -> IllegalStateException("HttpResponseValidator unexpected code $httpStatusCode")
    }
}

internal data class RedirectResponseException(val status: HttpStatusCode) : Exception() {
    constructor(exception: io.ktor.client.plugins.RedirectResponseException) : this(exception.response.status)
}

internal data class ClientRequestException(val status: HttpStatusCode) : Exception() {
    constructor(exception: io.ktor.client.plugins.ClientRequestException) : this(exception.response.status)
}

internal data class ServerResponseException(val status: HttpStatusCode) : Exception() {
    constructor(exception: io.ktor.client.plugins.ServerResponseException) : this(exception.response.status)
}