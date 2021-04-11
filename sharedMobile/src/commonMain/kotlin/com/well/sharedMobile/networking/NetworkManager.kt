package com.well.sharedMobile.networking

import com.well.modules.models.*
import com.well.modules.models.WebSocketMessage.*
import com.well.sharedMobile.networking.*
import com.well.sharedMobile.networking.NetworkManager.Status.*
import com.well.sharedMobile.networking.webSocketManager.WebSocketClient
import com.well.sharedMobile.networking.webSocketManager.WebSocketMessageListener
import com.well.sharedMobile.networking.webSocketManager.WebSocketSession
import com.well.sharedMobile.networking.webSocketManager.ws
import com.well.sharedMobile.puerh.call.resizedImage
import com.well.sharedMobile.utils.ImageContainer
import com.well.sharedMobile.utils.asImageContainer
import com.well.modules.atomic.Closeable
import com.well.modules.atomic.CloseableContainer
import com.well.modules.utils.base.MutableStateFlow
import com.well.modules.atomic.*
import com.well.modules.napier.Napier
import com.well.modules.utils.base.tryF
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json

class NetworkManager(
    token: String,
    startWebSocket: Boolean,
    private val unauthorizedHandler: () -> Unit,
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
    private val _currentUser = MutableStateFlow<User?>()

    val state = _state.asStateFlow()
    val onlineUsers = _onlineUsers.asStateFlow()
    val currentUser = _currentUser.asStateFlow()

    init {
        GlobalScope.launch {
            while (startWebSocket) {
                try {
                    _state.value = Connecting
                    client.ws("/mainWebSocket") {
                        webSocketSession.value = this
                        _state.value = Connected
                        for (string in incoming) {
                            Napier.i("websocket msg: $string")
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
                    if (handleUnauthorized(t)) break
                    Napier.e("web socket connection error", t)
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
        }.let {
            object : Closeable {
                override fun close() {
                    it.cancel()
                }
            }
        }.apply(::addCloseableChild)
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
            Napier.e("failed to send $message", t)
        }
    }

    suspend fun uploadImage(
        userId: UserId,
        image: ImageContainer
    ) = tryCheckAuth {
        client.client.post<String>("/uploadUserProfile") {
            body = MultiPartFormDataContent(
                formData {
                    var data: ByteArray
                    var quality = 1f
                    do {
                        data = image
                            .resizedImage(Size(2000))
                            .asByteArray(quality)
                        quality -= 0.2f
                        Napier.i("quality $quality ${data.count()}")
                    } while (data.count() > 250_000 && quality >= 0)
                    appendInput(
                        userId.toString(),
                        Headers.build {
                            append(
                                HttpHeaders.ContentDisposition,
                                "filename=uploadUserProfile.jpg"
                            )
                        },
                        data.size.toLong()
                    ) {
                        buildPacket { writeFully(data) }
                    }
                }
            )
        }
    }

    suspend fun putUser(user: User) = tryCheckAuth {
        client.client.put<Unit>("/user") {
            contentType(ContentType.Application.Json)
            body = user
        }
    }

    suspend fun downloadTestImage(): ImageContainer {
        try {
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

    private suspend fun <R> tryCheckAuth(
        block: suspend () -> R,
    ) = tryF(::handleUnauthorized, block = block)

    private fun handleUnauthorized(t: Throwable): Boolean {
        @Suppress("NAME_SHADOWING")
        when (val t = t.toResponseException()) {
            is ClientRequestException -> {
                if (t.status == HttpStatusCode.Unauthorized) {
                    unauthorizedHandler()
                    return true
                }
            }
        }
        return false
    }
}

private fun Throwable.toResponseException() : Throwable =
    when (this) {
        is io.ktor.client.features.RedirectResponseException -> RedirectResponseException(this)
        is io.ktor.client.features.ClientRequestException -> ClientRequestException(this)
        is io.ktor.client.features.ServerResponseException -> ServerResponseException(this)
        else -> this
    }

fun getThrowable(httpStatusCode: Int): Throwable? {
    val code = HttpStatusCode.fromValue(httpStatusCode)
    return when (httpStatusCode) {
        in 300..399 -> RedirectResponseException(code)
        in 400..499 -> ClientRequestException(code)
        in 500..599 -> ServerResponseException(code)
        else -> null
    }
}

data class RedirectResponseException(val status: HttpStatusCode): Exception() {
    constructor(exception: io.ktor.client.features.RedirectResponseException) : this(exception.response.status)
}

data class ClientRequestException(val status: HttpStatusCode): Exception() {
    constructor(exception: io.ktor.client.features.ClientRequestException) : this(exception.response.status)
}

data class ServerResponseException(val status: HttpStatusCode): Exception() {
    constructor(exception: io.ktor.client.features.ServerResponseException) : this(exception.response.status)
}