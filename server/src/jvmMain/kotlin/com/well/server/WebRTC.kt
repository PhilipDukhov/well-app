package com.well.server

//import com.well.serverModels.WebRTCMessage
//import com.well.serverModels.WebRTCMessage.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.cio.websocket.*
import java.time.Duration
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.webRTC(testing: Boolean = false) {
    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(ContentNegotiation) {
        json()
    }

    val connections = Collections.synchronizedMap(mutableMapOf<String, WebSocketServerSession>())
    val room = Collections.synchronizedList(mutableListOf<String>())

    routing {
        webSocket(path = "/socket") {
            val id = UUID.randomUUID().toString()
            connections[id] = this

            fun otherConnections(): Collection<WebSocketServerSession> =
                connections.filter { it.key != id }.values

            try {
//                for (data in incoming) {
//                    when (data) {
//                        is Frame.Ping -> {
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
//                        }
//                        else -> otherConnections().forEach { it.send(data) }
//                    }
//                }
            } finally {
                connections.remove(id)
            }
        }
    }
}

//private suspend fun WebSocketServerSession.send(message: WebRTCMessage) {
//    send(Json.encodeToString(message))
//}

