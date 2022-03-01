package com.well.modules.networking

import io.ktor.http.*

internal fun Throwable.toResponseException(): Throwable =
    when (this) {
        is io.ktor.client.features.RedirectResponseException -> RedirectResponseException(this)
        is io.ktor.client.features.ClientRequestException -> ClientRequestException(this)
        is io.ktor.client.features.ServerResponseException -> ServerResponseException(this)
        else -> this
    }

internal fun getThrowable(httpStatusCode: Int): Throwable? {
    val code = HttpStatusCode.fromValue(httpStatusCode)
    return when (httpStatusCode) {
        in 300..399 -> RedirectResponseException(code)
        in 400..499 -> ClientRequestException(code)
        in 500..599 -> ServerResponseException(code)
        else -> null
    }
}

internal data class RedirectResponseException(val status: HttpStatusCode) : Exception() {
    constructor(exception: io.ktor.client.features.RedirectResponseException) : this(exception.response.status)
}

internal data class ClientRequestException(val status: HttpStatusCode) : Exception() {
    constructor(exception: io.ktor.client.features.ClientRequestException) : this(exception.response.status)
}

internal data class ServerResponseException(val status: HttpStatusCode) : Exception() {
    constructor(exception: io.ktor.client.features.ServerResponseException) : this(exception.response.status)
}