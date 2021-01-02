package com.well.serverModels

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

inline fun createBaseHttpClient() = HttpClient {
    install(JsonFeature) {
        serializer = KotlinxSerializer(
            kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            }
        )
    }
    HttpResponseValidator {
        validateResponse { response ->
            val status = response.status
            if (status.value < 300) return@validateResponse

            val content = if (response.content.availableForRead > 0) {
                response.readBytes()
                    .toString()
            } else {
                ""
            }
            throw Throwable("HttpResponseValidator ${response.request.url} $status $content")
        }
    }
}