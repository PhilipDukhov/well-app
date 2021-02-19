package com.well.utils

import io.ktor.client.engine.ios.*

actual fun Throwable.userReadableDescription(): String? = when (this) {
    is IosHttpRequestException -> {
        origin.localizedDescription
    }
    else -> null
}
