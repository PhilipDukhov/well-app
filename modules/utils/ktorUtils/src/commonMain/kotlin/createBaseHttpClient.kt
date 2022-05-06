package com.well.modules.utils.ktorUtils

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.statement.*

fun createBaseHttpClient(): HttpClient = HttpClient {
    install(JsonFeature) {
        serializer = KotlinxSerializer(
            kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            }
        )
    }
    install(Logging) {
        level = LogLevel.NONE
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
            throw Exception("HttpResponseValidator ${response.request.url} $status $content")
        }
    }
}