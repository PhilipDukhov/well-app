package com.well.modules.utils.ktorUtils

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun createBaseHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(
            Json {
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

            val content = response.bodyAsText()
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