package com.well.modules.networking

import com.well.modules.utils.ktorUtils.UnauthorizedException
import io.ktor.http.*

internal fun Exception.toResponseException(): Exception =
    when (this) {
        is io.ktor.client.features.RedirectResponseException -> RedirectResponseException(this)
        is io.ktor.client.features.ClientRequestException -> ClientRequestException(this)
        is io.ktor.client.features.ServerResponseException -> ServerResponseException(this)
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

internal class RedirectResponseException(val status: HttpStatusCode, cause: Exception? = null) : Exception(cause) {
    constructor(exception: io.ktor.client.features.RedirectResponseException) : this(exception.response.status, exception)
}

internal class ClientRequestException(val status: HttpStatusCode, cause: Exception? = null) : Exception(cause) {
    constructor(exception: io.ktor.client.features.ClientRequestException) : this(exception.response.status, exception)
}

internal class ServerResponseException(val status: HttpStatusCode, cause: Exception? = null) : Exception(cause) {
    constructor(exception: io.ktor.client.features.ServerResponseException) : this(exception.response.status, exception)
}