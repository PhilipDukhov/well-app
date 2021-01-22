package com.well.sharedMobile.networking.webSocketManager

import com.github.aakira.napier.Napier
import com.well.serverModels.User
import com.well.serverModels.WebSocketMessage
import com.well.serverModels.WebSocketMessage.*
import com.well.serverModels.createBaseHttpClient
import com.well.sharedMobile.networking.*
import com.well.sharedMobile.networking.WebSocketClient
import com.well.sharedMobile.networking.webSocketManager.NetworkManager.Status.*
import com.well.sharedMobile.networking.ws
import com.well.sharedMobile.utils.ImageContainer
import com.well.sharedMobile.utils.asImageContainer
import com.well.utils.Closeable
import com.well.utils.CloseableContainer
import com.well.utils.atomic.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.bits.*
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

    suspend fun send(message: WebSocketMessage) {
        try {
            webSocketSession.value?.send(
                Json.encodeToString(
                    WebSocketMessage.serializer(),
                    message
                )
            )
        } catch (t: Throwable) {
            println("failed to send $message $t")
        }
    }

    suspend fun downloadTestImage(): ImageContainer {
        try {
//            val statement = httpClient.request<HttpStatement>(url) {
//                range?.let {
//                    header(HttpHeaders.Range, range)
//                }
//            }
            val url = currentUser.value!!.profileImageUrl!!
            val statement = createBaseHttpClient()
                .get<HttpStatement>(url)
            return statement.execute {
                val contentLength = it.contentLength()?.lowInt ?: 0
                val byteArray = ByteArray(contentLength)
                var offset = 0
                do {
                    val currentRead = it.content.readAvailable(
                        byteArray,
                        offset,
                        byteArray.size - offset
                    )
                    offset += currentRead
                } while (offset < contentLength)
                return@execute byteArray
            }.asImageContainer()
        } catch (t: Throwable) {
            throw t
        }
    }
}