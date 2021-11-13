package com.well.server.routing

import com.well.modules.models.User
import com.well.modules.models.WebSocketMsg
import com.well.server.utils.Dependencies
import com.well.server.utils.authUid
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

suspend fun DefaultWebSocketServerSession.mainWebSocket(dependencies: Dependencies) {
    val currentUid = call.authUid
    try {
        val currentUserSession = UserSession(
            currentUid = currentUid,
            webSocketSession = this,
            dependencies = dependencies,
        )
        dependencies.database.usersQueries.updateLastOnline(currentUid)
        dependencies.connectedUserSessionsFlow.put(currentUid, currentUserSession)
        incoming@ for (frame in incoming) when (frame) {
            is Frame.Text -> Json.decodeFromString<WebSocketMsg>(frame.readText())
                .let { msg ->
                    println("$currentUid $msg")
                    when (msg) {
                        is WebSocketMsg.Back,
                        -> throw IllegalStateException("$msg can't be sent by users")
                        is WebSocketMsg.Call -> {
                            currentUserSession.handleCallMsg(msg)
                        }
                        is WebSocketMsg.Front -> {
                            currentUserSession.handleFrontMsg(msg)
                        }
                    }
                }
            else -> Unit
        }
    } catch (t: Throwable) {
        println("mainWebSocket closed with error: $t\n${t.stackTraceToString()}")
    } finally {
        dependencies.userDisconnected(currentUid)
    }
}

private suspend fun Dependencies.userDisconnected(
    uid: User.Id,
) {
    connectedUserSessionsFlow.remove(uid)
    database.usersQueries.updateLastOnline(uid)

    // notify call users that current if offline
    // TODO: wait user to reconnect
    endCall(uid, WebSocketMsg.Call.EndCall.Reason.Offline)
}

