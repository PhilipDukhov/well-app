package com.well.server.routing

import com.well.modules.models.DeviceId
import com.well.modules.models.WebSocketMsg
import com.well.server.utils.ClientKey
import com.well.server.utils.Dependencies
import com.well.server.utils.authUid
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

suspend fun DefaultWebSocketServerSession.mainWebSocket(dependencies: Dependencies, deviceId: DeviceId) {
    val currentUid = call.authUid
    val clientKey = ClientKey(deviceId, currentUid)
    try {
        val user = dependencies.database.usersQueries.getById(currentUid)
            .executeAsOne()
        println("mainWebSocket connected $user")
        val currentUserSession = UserSession(
            currentUid = currentUid,
            deviceId = deviceId,
            webSocketSession = this,
            dependencies = dependencies,
        )
        dependencies.database.usersQueries.updateLastOnline(currentUid)
        dependencies.connectedUserSessionsFlow.put(clientKey, currentUserSession)
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
    } catch (e: Exception) {
        println("mainWebSocket closed with error: $e\n${e.stackTraceToString()}")
    } finally {
        println("mainWebSocket disconnected $clientKey")
        dependencies.userDisconnected(clientKey)
    }
}

private suspend fun Dependencies.userDisconnected(
    clientKey: ClientKey,
) {
    connectedUserSessionsFlow.remove(clientKey)
    database.usersQueries.updateLastOnline(clientKey.uid)

    // notify call users that current if offline
    // TODO: wait user to reconnect
    endCall(clientKey.uid, WebSocketMsg.Call.EndCall.Reason.Offline)
}

