package com.well.server.utils

import com.well.modules.models.UserId
import com.well.modules.models.WebSocketMessage
import com.well.modules.models.createBaseHttpClient
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

    val client = createBaseHttpClient()

    suspend fun getRandomPicture() =
        client.get<HttpStatement>("https://picsum.photos/1000")
            .execute { it.request.url }

    private fun onlineUsers() = if (connectedUserSessions.keys.isEmpty()) listOf() else
        database
            .userQueries
            .getByIds(connectedUserSessions.keys)
            .executeAsList()
            .map { it.toUser() }

    suspend fun notifyUserUpdated(id: UserId) {
        connectedUserSessions[id]?.let { session ->
            session.send(
                WebSocketMessage.CurrentUser(
                    database
                        .userQueries
                        .getById(id)
                        .executeAsOne()
                        .toUser()
                )
            )
            notifyOnline()
        }
    }

    suspend fun notifyOnline() = connectedUserSessions.run {
        val users = onlineUsers()
        forEach { sessionPair ->
            sessionPair
                .value
                .send(
                    WebSocketMessage.OnlineUsersList(
                        users.filter { it.id != sessionPair.key }
                    )
                )
        }
    }

    fun awsProfileImagePath(
        userId: UserId,
        ext: String
    ) = "profilePictures/$userId-${UUID.randomUUID()}.$ext"
}