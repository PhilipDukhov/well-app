package com.well.server.utils

import com.well.modules.models.UserId
import com.well.modules.models.WebSocketMsg
import com.well.modules.models.createBaseHttpClient
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import java.util.*

class Dependencies(app: Application) {
    val environment = app.environment
    val database = initialiseDatabase(app)
    val jwtConfig = JwtConfig(
        environment.configProperty("jwt.accessTokenSecret"),
    )
    val awsManager: AwsManager by lazy {
        AwsManager(
            environment.configProperty("aws.accessKeyId"),
            environment.configProperty("aws.secretAccessKey"),
            environment.configProperty("aws.bucketName"),
        )
    }
    val connectedUserSessions: MutableMap<UserId, WebSocketSession> = Collections.synchronizedMap(
        mutableMapOf<UserId, WebSocketSession>()
    )

    val client = createBaseHttpClient()

    suspend fun notifyCurrentUserUpdated(id: UserId) {
        connectedUserSessions[id]?.let { session ->
            session.send(
                WebSocketMsg.CurrentUser(
                    getCurrentUser(id).also {
                        println("getCurrentUser $it")
                    }
                )
            )
            notifyOnline()
        }
    }

    suspend fun notifyOnline() = connectedUserSessions.run {
//        val users = onlineUsers()
//        forEach { sessionPair ->
//            sessionPair
//                .value
//                .send(
//                    WebSocketMsg.OnlineUsersList(
//                        users.filter { it.id != sessionPair.key }
//                    )
//                )
//        }
    }

    fun awsProfileImagePath(
        uid: UserId,
        ext: String
    ) = "profilePictures/$uid-${UUID.randomUUID()}.$ext"

    fun getCurrentUser(id: UserId) = getUser(uid = id, currentUid = id)

    fun getUser(
        uid: UserId,
        currentUid: UserId,
    ) = database
        .usersQueries
        .getById(uid)
        .executeAsOne()
        .toUser(
            currentUid = currentUid,
            database = database,
        )
}