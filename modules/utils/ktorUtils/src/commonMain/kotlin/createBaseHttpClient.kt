package com.well.modules.utils.ktorUtils

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.statement.*
import io.ktor.http.*

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
            throw when (status.value) {
                HttpStatusCode.Unauthorized.value -> UnauthorizedException()
                in 300..399 -> RedirectResponseException(response, content)
                in 400..499 -> ClientRequestException(response, content)
                in 500..599 -> ServerResponseException(response, content)
                else -> IllegalStateException("HttpResponseValidator unexpected code ${status.value} $response")
            }
        }
    }
}