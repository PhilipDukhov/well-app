package com.well.server.routing

import com.well.server.utils.Dependencies
import com.well.server.utils.authUserId
import com.well.server.utils.send
import com.well.server.utils.toUser
import com.well.serverModels.UserId
import com.well.serverModels.WebSocketMessage
import com.well.serverModels.WebSocketMessage.EndCall.Reason.Busy
import com.well.serverModels.WebSocketMessage.EndCall.Reason.Offline
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

data class Call(val userIds: List<UserId>) {
    constructor(vararg userIds: UserId) : this(userIds.toList())
}

suspend fun DefaultWebSocketServerSession.mainWebSocket(dependencies: Dependencies) {
    val currentUserId = call.authUserId
    try {
        dependencies.connectedUserSessions[currentUserId] = this
        dependencies.notifyUserUpdated(currentUserId)
        incoming@ for (frame in incoming) when (frame) {
            is Frame.Text -> Json.decodeFromString<WebSocketMessage>(frame.readText())
                .let { msg ->
                    when (msg) {
                        is WebSocketMessage.OnlineUsersList,
                        is WebSocketMessage.IncomingCall,
                        is WebSocketMessage.CurrentUser,
                        -> throw IllegalStateException("$msg can't be sent by users")
                        is WebSocketMessage.InitiateCall -> {
                            val session = dependencies.connectedUserSessions[msg.userId]
                            if (session == null) {
                                send(WebSocketMessage.EndCall(Offline))
                                return
                            }
                            if (dependencies.calls.any { it.userIds.contains(currentUserId) }) {
                                send(WebSocketMessage.EndCall(Busy))
                                return
                            }
                            session.send(
                                WebSocketMessage.IncomingCall(
                                    dependencies
                                        .database
                                        .userQueries
                                        .getById(currentUserId)
                                        .executeAsOne()
                                        .toUser(),
                                )
                            )
                            dependencies.calls.add(Call(currentUserId, msg.userId))
                        }
                        is WebSocketMessage.Answer,
                        is WebSocketMessage.Offer,
                        is WebSocketMessage.Candidate ->
                            dependencies.callPartnerId(currentUserId)!!
                                .run {
                                    dependencies.connectedUserSessions[value]!!
                                        .send(msg)
                                }
                        is WebSocketMessage.EndCall -> dependencies.endCall(
                            currentUserId,
                            msg.reason
                        )
                    }
                }
            else -> Unit
        }
    } catch (t: Throwable) {
        println("mainWebSocket $t ${t.stackTraceToString()}")
    } finally {
        dependencies.userDisconnected(currentUserId)
    }
}

private suspend fun Dependencies.userDisconnected(
    userId: UserId
) {
    connectedUserSessions.remove(userId)
    notifyOnline()

    // notify call users that current if offline
    // TODO: wait user to reconnect
    endCall(userId, Offline)
}

private fun Dependencies.callPartnerId(userId: UserId) =
    calls
        .withIndex()
        .firstOrNull { it.value.userIds.contains(userId) }
        ?.run {
            IndexedValue(
                index,
                value
                    .userIds
                    .first { it != userId }
            )
        }

private suspend fun Dependencies.endCall(
    userId: UserId,
    reason: WebSocketMessage.EndCall.Reason,
) = callPartnerId(userId)
    ?.run {
        connectedUserSessions[value]!!.send(WebSocketMessage.EndCall(reason))
        calls.removeAt(index)
    }

