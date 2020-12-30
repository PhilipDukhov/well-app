package com.well.server.utils

import com.well.server.routing.Call
import com.well.serverModels.UserId
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import java.util.*

class Dependencies(app: Application) {
    val environment = app.environment
    val database = initialiseDatabase(app)
    val jwtConfig = JwtConfig(
        environment.configProperty("jwt.accessTokenSecret"),
    )
    val awsManager = AwsManager(
        environment.configProperty("aws.accessKeyId"),
        environment.configProperty("aws.secretAccessKey"),
        environment.configProperty("aws.bucketName"),
    )
    val connectedUserSessions = Collections.synchronizedMap(mutableMapOf<UserId, WebSocketSession>())
    val calls = Collections.synchronizedList(mutableListOf<Call>())

    val localClient = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(
                kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                }
            )
        }
        HttpResponseValidator {
            validateResponse { response ->
                val status = response.status.value
                if (status < 300) return@validateResponse

                val responseString = String(response.readBytes())
                val exceptionResponse = Json.parseToJsonElement(responseString)

                when (status) {
                    in 300..399 -> throw Throwable(exceptionResponse.toString())
                    in 400..499 -> throw Throwable(exceptionResponse.toString())
                    in 500..599 -> throw Throwable(exceptionResponse.toString())
                    else -> throw Throwable(exceptionResponse.toString())
                }
            }
        }
    }

    suspend fun getRandomPicture() =
        localClient.get<HttpStatement>("https://picsum.photos/1000")
            .execute { it.request.url }
}