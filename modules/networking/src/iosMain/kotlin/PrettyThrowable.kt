package com.well.modules.networking

import io.ktor.client.engine.darwin.*

actual fun Throwable.userReadableDescription(): String? = when (this) {
    is DarwinHttpRequestException -> {
        origin.localizedDescription
    }
    else -> null
}