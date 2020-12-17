package com.well.server.routing

import com.well.server.utils.Dependencies
import com.well.server.utils.authUserId
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*

suspend fun DefaultWebSocketServerSession.call(dependencies: Dependencies) {
    val id = call.authUserId
    dependencies.connectedUserSessions[id] = this

    fun otherConnections() =
        dependencies.connectedUserSessions.filter { it.key != id }.values

    try {
        for (data in incoming) {
            when (data) {
                is Frame.Ping -> {

//                            if (room.isEmpty()) {
//                                room.add(id)
//                                send(Created)
//                            } else if (room.size == 1) {
//                                otherConnections().forEach {
//                                    it.send(Join)
//                                }
//                                room.add(id)
//                                send(Joined)
//                                println("->$id $Joined")
//                            }
                }
                else -> otherConnections().forEach { it.send(data) }
            }
        }
    } finally {
        dependencies.connectedUserSessions.remove(id)
    }
}


