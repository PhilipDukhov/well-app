package com.well.server.utils

import com.well.server.routing.Call
import com.well.serverModels.UserId
import com.well.serverModels.createBaseHttpClient
import io.ktor.application.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.cio.websocket.*
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
    val connectedUserSessions: MutableMap<UserId, WebSocketSession> = Collections.synchronizedMap(
        mutableMapOf<UserId, WebSocketSession>()
    )
    val calls: MutableList<Call> = Collections.synchronizedList(mutableListOf<Call>())

    val client = createBaseHttpClient()

    suspend fun getRandomPicture() =
        client.get<HttpStatement>("https://picsum.photos/1000")
            .execute { it.request.url }
}