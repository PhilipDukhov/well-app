package com.well.server.routing

import com.well.server.utils.Dependencies
import com.well.server.utils.authUserId
import com.well.server.utils.toUser
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

suspend fun DefaultWebSocketServerSession.onlineUsers(dependencies: Dependencies) {
    val currentUserId = call.authUserId
    dependencies.connectedUserSessions[currentUserId] = this
    println("${dependencies.connectedUserSessions}")
    suspend fun notify(includeSelf: Boolean) {
        dependencies.connectedUserSessions.apply {
            if (isEmpty()) return
            val users = dependencies
                .database
                .userQueries
                .getByIds(keys)
                .executeAsList()
                .map { it.toUser() }
            (if (includeSelf) this else filter { it.key != currentUserId })
                .forEach { sessionPair ->
                    sessionPair
                        .value
                        .send(
                            Frame.Text(
                                Json.encodeToString(
                                    users.filter { it.id != sessionPair.key }
                                )
                            )
                        )
                }
        }
    }
    notify(true)
    try {
        for (data in incoming) {
        }
    } finally {
        dependencies.connectedUserSessions.remove(currentUserId)
        notify(false)
    }
}