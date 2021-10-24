package com.well.modules.models

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

fun createBaseHttpClient() = HttpClient {
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
            throw Throwable("HttpResponseValidator ${response.request.url} $status $content")
        }
    }
}

open class BaseConstants {
    val oauthCallbackProtocol = "world.endo.live.link"
    fun oauthCallbackPath(path: String = "") = "$oauthCallbackProtocol://$path"
}

inline fun <reified T : Enum<T>> T.spacedUppercaseName(): String =
    name.mapIndexed { i, c ->
        if (i == 0 || c.isLowerCase() || name[i - 1].isUpperCase()) return@mapIndexed c.toString()
        " $c"
    }.joinToString(separator = "")

inline fun <reified T : Enum<T>> spacedUppercaseEnumValues() =
    enumValues<T>().map { it.spacedUppercaseName() }

inline fun <reified T : Enum<T>> enumValueOfSpacedUppercase(name: String): T =
    enumValues<T>().first { it.spacedUppercaseName() == name }

expect fun Char.isUpperCase(): Boolean
expect fun Char.isLowerCase(): Boolean