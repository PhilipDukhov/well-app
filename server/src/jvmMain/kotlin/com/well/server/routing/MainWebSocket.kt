package com.well.server.routing

import com.well.server.utils.Dependencies
import com.well.server.utils.authUid
import com.well.server.utils.send
import com.well.modules.models.UserId
import com.well.modules.models.WebSocketMsg
import com.well.modules.models.WebSocketMsg.EndCall.Reason.Busy
import com.well.modules.models.WebSocketMsg.EndCall.Reason.Offline
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
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
        dependencies.connectedUserSessions[currentUid] = this
        dependencies.notifyCurrentUserUpdated(currentUid)
        incoming@ for (frame in incoming) when (frame) {
            is Frame.Text -> Json.decodeFromString<WebSocketMsg>(frame.readText())
                .let { msg ->
                    when (msg) {
                        is WebSocketMsg.IncomingCall,
                        is WebSocketMsg.CurrentUser,
                        -> throw IllegalStateException("$msg can't be sent by users")
                        is WebSocketMsg.InitiateCall -> {
                            val session = dependencies.connectedUserSessions[msg.uid]
                            if (session == null) {
                                send(WebSocketMsg.EndCall(Offline))
                                return
                            }
                            if (calls.any { it.uids.contains(currentUid) }) {
                                send(WebSocketMsg.EndCall(Busy))
                                return
                            }
                            session.send(
                                WebSocketMsg.IncomingCall(
                                    dependencies.getUser(
                                        uid = msg.uid,
                                        currentUid = currentUid,
                                    )
                                )
                            )
                            calls.add(Call(currentUid, msg.uid))
                        }
                        is WebSocketMsg.Answer,
                        is WebSocketMsg.Offer,
                        is WebSocketMsg.Candidate ->
                            dependencies.callPartnerId(currentUid)!!
                                .run {
                                    dependencies.connectedUserSessions[value]!!
                                        .send(msg)
                                }
                        is WebSocketMsg.EndCall -> dependencies.endCall(
                            currentUid,
                            msg.reason
                        )
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
    connectedUserSessions.remove(uid)
    notifyOnline()

    // notify call users that current if offline
    // TODO: wait user to reconnect
    endCall(uid, Offline)
}

private fun Dependencies.callPartnerId(uid: UserId) =
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
    reason: WebSocketMsg.EndCall.Reason,
) = callPartnerId(uid)
    ?.run {
        connectedUserSessions[value]!!.send(WebSocketMsg.EndCall(reason))
        calls.removeAt(index)
    }

