package com.well.shared.puerh

import com.github.aakira.napier.Napier
import com.well.serverModels.User
import com.well.serverModels.WebSocketMessage
import com.well.serverModels.WebSocketMessage.*
import com.well.shared.networking.WebSocketClient
import com.well.shared.networking.ws
import com.well.shared.puerh.WebSocketManager.Status.*
import com.well.utils.Closeable
import com.well.utils.CloseableContainer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class WebSocketManager(
    token: String
): CloseableContainer() {
    private val client = WebSocketClient(
        WebSocketClient.Config(
            "dukhovwellserver.com",
            8090,
            token,
        )
    )

    enum class Status {
        Disconnected,
        Connecting,
        Connected,
    }

    private val _state = MutableStateFlow(Disconnected)
    private val _onlineUsers = MutableStateFlow(listOf<User>())

    val state = _state.asStateFlow()
    val onlineUsers = _onlineUsers.asStateFlow()

    private var connectToServerCloseable: Closeable? = null

    fun connect() {
        if (_state.value != Disconnected) return
        connectToServerCloseable?.close()
        println("tesststst0 connectttt")
        connectToServerCloseable = GlobalScope.launch {
            try {
                _state.value = Connecting
                client.ws("/onlineUsers") {
                    println("tesststst0 connected")
                    _state.value = Connected
                    for (string in incoming) {
                        val message = Json.decodeFromString<WebSocketMessage>(string)
                        when (message) {
                            is OnlineUsersList -> {
                                println("tesststst1 ${message.users}")
                                _onlineUsers.emit(message.users)
                            }
                            is InitiateCall -> TODO()
                            is AcceptCall -> TODO()
                            is EndCall -> TODO()
                        }
                    }
                }
            } catch (t: Throwable) {
                Napier.e("tesststst-1 web socket error: $t")
            } finally {
                _onlineUsers.emit(emptyList())
                _state.emit(Disconnected)
                connectToServerCloseable = null
            }
        }.let {
            object : Closeable {
                override fun close() {
                    it.cancel()
                }
            }
        }.apply(::addCloseableChild)
    }
}