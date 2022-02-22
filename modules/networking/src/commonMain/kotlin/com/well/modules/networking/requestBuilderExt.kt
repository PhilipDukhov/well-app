package com.well.modules.networking

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.core.*

internal fun HttpRequestBuilder.multipartFormBody(builder: FormBuilder.() -> Unit) {
    body = MultiPartFormDataContent(formData(builder))
}

internal fun FormBuilder.appendByteArrayInput(
    data: ByteArray,
    key: String? = null,
    filename: String = "someName",
) = appendInput(
    key = key ?: "key",
    headers = Headers.build {
        append(
            HttpHeaders.ContentDisposition,
            "filename=$filename"
        )
    },
    size = data.size.toLong()
) {
    buildPacket { writeFully(data) }
}