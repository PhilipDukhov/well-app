package com.well.server.routing

import com.well.modules.models.UserId
import com.well.modules.models.WebSocketMsg
import com.well.server.routing.user.UserSession
import com.well.server.utils.Dependencies
import com.well.server.utils.authUid
import com.well.server.utils.send
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*

data class Call(val uids: List<UserId>) {
    constructor(vararg uids: UserId) : this(uids.toList())
}

private val calls: MutableList<Call> = Collections.synchronizedList(mutableListOf<Call>())

suspend fun DefaultWebSocketServerSession.mainWebSocket(dependencies: Dependencies) {
    val currentUid = call.authUid
    try {
        val currentUserSession = UserSession(
            currentUid = currentUid,
            webSocketSession = this,
            dependencies = dependencies,
        )
        dependencies.connectedUserSessions[currentUid] = currentUserSession
        dependencies.notifyCurrentUserUpdated(currentUid)
        incoming@ for (frame in incoming) when (frame) {
            is Frame.Text -> Json.decodeFromString<WebSocketMsg>(frame.readText())
                .let { msg ->
                    println("$currentUid $msg")
                    when (msg) {
                        is WebSocketMsg.Back,
                        -> throw IllegalStateException("$msg can't be sent by users")
                        is WebSocketMsg.Front.SetExpertsFilter -> {
                            println("emit expertsFilterFlow ${msg.filter}")
                            currentUserSession.expertsFilterFlow.emit(msg.filter)
                        }
                        is WebSocketMsg.Front.SetUsersPresence -> {
                            currentUserSession.usersPresenceInfoFlow.emit(msg.usersPresence)
                        }
                        is WebSocketMsg.Front.InitiateCall -> {
                            val session = dependencies.connectedUserSessionsFlow[msg.uid]
                            if (session == null) {
                                send(WebSocketMsg.Call.EndCall(WebSocketMsg.Call.EndCall.Reason.Offline))
                                return
                            }
                            if (calls.any { it.uids.contains(currentUid) }) {
                                send(WebSocketMsg.Call.EndCall(WebSocketMsg.Call.EndCall.Reason.Busy))
                                return
                            }
                            session.send(
                                WebSocketMsg.Back.IncomingCall(
                                    dependencies.getUser(
                                        uid = msg.uid,
                                        currentUid = currentUid,
                                    )
                                )
                            )
                            calls.add(Call(currentUid, msg.uid))
                        }
                        is WebSocketMsg.Call -> {
                            callPartnerId(currentUid)!!
                                .run {
                                    dependencies.connectedUserSessionsFlow[value]!!
                                        .send(msg)
                                }
                        }
                        is WebSocketMsg.Call.EndCall -> {
                            dependencies.endCall(
                                currentUid,
                                msg.reason
                            )
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
    uid: UserId
) {
    connectedUserSessionsFlow.remove(uid)
    database.usersQueries.updateLastOnline(uid)

    // notify call users that current if offline
    // TODO: wait user to reconnect
    endCall(uid, WebSocketMsg.Call.EndCall.Reason.Offline)
}

private fun callPartnerId(uid: UserId) =
    calls
        .withIndex()
        .firstOrNull { it.value.uids.contains(uid) }
        ?.run {
            IndexedValue(
                index,
                value
                    .uids
                    .first { it != uid }
            )
        }

private suspend fun Dependencies.endCall(
    uid: UserId,
    reason: WebSocketMsg.Call.EndCall.Reason,
) = callPartnerId(uid)
    ?.run {
        connectedUserSessionsFlow[value]!!.send(WebSocketMsg.Call.EndCall(reason))
        calls.removeAt(index)
    }

