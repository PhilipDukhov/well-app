package com.well.modules.utils.viewUtils

import io.ktor.client.engine.ios.*

actual fun Throwable.userReadableDescription(): String? = when (this) {
    is IosHttpRequestException -> {
        origin.localizedDescription
    }
    else -> null
}