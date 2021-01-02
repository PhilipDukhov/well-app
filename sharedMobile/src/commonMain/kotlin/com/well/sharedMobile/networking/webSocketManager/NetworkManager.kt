package com.well.sharedMobile.networking.webSocketManager

import com.github.aakira.napier.Napier
import com.well.serverModels.User
import com.well.serverModels.WebSocketMessage
import com.well.serverModels.WebSocketMessage.*
import com.well.sharedMobile.networking.Constants
import com.well.sharedMobile.networking.WebSocketClient
import com.well.sharedMobile.networking.WebSocketSession
import com.well.sharedMobile.networking.ws
import com.well.sharedMobile.networking.webSocketManager.NetworkManager.Status.*
import com.well.utils.Closeable
import com.well.utils.CloseableContainer
import com.well.utils.atomic.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json

class NetworkManager(
    token: String
) : CloseableContainer() {
    private val client = WebSocketClient(
        WebSocketClient.Config(
            Constants.host,
            Constants.port,
            token,
        )
    )

    enum class Status {
        Disconnected,
        Connecting,
        Connected,
        ;

        val stringRepresentation: String
            get() = when (this) {
                Disconnected -> "Disconnected"
                Connecting -> "Connecting"
                Connected -> "Connected"
            }
    }

    private val webSocketSession = AtomicRef<WebSocketSession?>(null)
    private val listeners = AtomicMutableList<WebSocketMessageListener>()

    private val _state = MutableStateFlow(Disconnected)
    private val _onlineUsers = MutableStateFlow(listOf<User>())
    private val _currentUser = MutableStateFlow<User?>(null)

    val state = _state.asStateFlow()
    val onlineUsers = _onlineUsers.asStateFlow()
    val currentUser = _currentUser.asStateFlow()

    init {
        GlobalScope.launch {
            while (true) {
                try {
                    _state.value = Connecting
                    client.ws("/mainWebSocket") {
                        webSocketSession.value = this
                        _state.value = Connected
                        for (string in incoming) {
                            println("websocket msg: $string")
                            val msg = Json.decodeFromString(
                                WebSocketMessage.serializer(),
                                string
                            )
                            when (msg) {
                                is OnlineUsersList ->
                                    _onlineUsers.emit(msg.users)
                                is CurrentUser ->
                                    _currentUser.value = msg.user
                                else ->
                                    listeners.notifyAll(msg)
                            }
                        }
                    }
                } catch (t: Throwable) {
                    Napier.e("web socket error: $t")
                } finally {
                    _onlineUsers.emit(emptyList())
                    val wasConnected = _state.value == Connected
                    _state.value = Disconnected
                    webSocketSession.value = null
                    if (!wasConnected) {
                        delay(5000L)
                    }
                }
            }
        }
            .let {
                object : Closeable {
                    override fun close() {
                        it.cancel()
                    }
                }
            }
            .apply(::addCloseableChild)
    }

    fun addListener(listener: WebSocketMessageListener): Closeable =
        listeners.addListenerAndMakeCloseable(listener)
            .also(::addCloseableChild)

    suspend fun send(message: WebSocketMessage) =
        webSocketSession.value?.send(
            Json.encodeToString(
                WebSocketMessage.serializer(),
                message
            )
        )
}