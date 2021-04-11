package com.well.modules.utils

import io.ktor.client.engine.ios.*

actual fun Throwable.userReadableDescription(): String? = when (this) {
    is IosHttpRequestException -> {
        origin.localizedDescription
    }
    else -> null
}
