package com.well.modules.networking

import com.well.modules.atomic.AtomicRef
import com.well.modules.atomic.Closeable
import com.well.modules.atomic.CloseableContainer
import com.well.modules.models.FavoriteSetter
import com.well.modules.models.RatingRequest
import com.well.modules.models.Size
import com.well.modules.models.User
import com.well.modules.models.UserId
import com.well.modules.models.WebSocketMsg
import com.well.modules.utils.sharedImage.ImageContainer
import com.well.modules.utils.tryF
import com.well.modules.networking.NetworkManager.Status.Connected
import com.well.modules.networking.NetworkManager.Status.Connecting
import com.well.modules.networking.NetworkManager.Status.Disconnected
import com.well.modules.networking.webSocketManager.WebSocketClient
import com.well.modules.networking.webSocketManager.WebSocketSession
import com.well.modules.networking.webSocketManager.ws
import com.well.modules.features.call.resizedImage
import io.github.aakira.napier.Napier
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class NetworkManager(
    token: String,
    startWebSocket: Boolean,
    private val unauthorizedHandler: () -> Unit,
) : CloseableContainer() {
    private val client = WebSocketClient(
        WebSocketClient.Config(
            host = Constants.host,
            port = Constants.port,
            webSocketProtocol = Constants.webSocketProtocol,
            bearerToken = token,
        )
    )

    enum class Status {
        Disconnected,
        Connecting,
        Connected,
        ;

        val stringRepresentation: String
            get() = name
    }

    private val webSocketScope = CoroutineScope(Dispatchers.Default)

    private var webSocketSession by AtomicRef<WebSocketSession?>(null)

    private val _webSocketMsgSharedFlow = MutableSharedFlow<WebSocketMsg>()
    val webSocketMsgSharedFlow: Flow<WebSocketMsg> = _webSocketMsgSharedFlow

    private val _state = MutableStateFlow(Disconnected)

    val stateFlow = _state.asStateFlow()
    val isConnectedFlow = stateFlow.map { it == Connected }.distinctUntilChanged()
    val onConnectedFlow = isConnectedFlow.filter { it }.map { }

    init {
        webSocketScope.launch {
            while (startWebSocket) {
                try {
                    _state.value = Connecting
                    client.ws("/mainWebSocket") {
                        webSocketSession = this
                        _state.value = Connected
                        for (string in incoming) {
                            Napier.i("websocket msg: $string")
                            _webSocketMsgSharedFlow.emit(
                                Json.decodeFromString(
                                    WebSocketMsg.serializer(),
                                    string
                                )
                            )
                        }
                    }
                } catch (t: Throwable) {
                    if (handleUnauthorized(t)) break
                    Napier.e("web socket connection error", t)
                } finally {
                    val wasConnected = _state.value == Connected
                    _state.value = Disconnected
                    webSocketSession = null
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

    suspend fun send(msg: WebSocketMsg.Front) = send(msg as WebSocketMsg)

    suspend fun send(msg: WebSocketMsg.Call) = send(msg as WebSocketMsg)

    private suspend fun send(msg: WebSocketMsg) {
        try {
            Napier.i("send WebSocketMsg: $msg")
            webSocketSession?.send(
                Json.encodeToString(
                    WebSocketMsg.serializer(),
                    msg
                )
            )
        } catch (t: Throwable) {
            Napier.e("failed to send $msg", t)
        }
    }

    suspend fun uploadProfilePicture(
        userId: UserId,
        image: ImageContainer
    ) = tryCheckAuth {
        client.client.post<String>("/user/uploadProfilePicture") {
            body = image.toMultiPartFormDataContent(userId.toString())
        }
    }

    suspend fun uploadMessagePicture(
        image: ImageContainer
    ) = tryCheckAuth {
        client.client.post<String>("uploadMessageMedia") {
            body = image.toMultiPartFormDataContent()
        }
    }

    private fun ImageContainer.toMultiPartFormDataContent(key: String? = null) =
        MultiPartFormDataContent(
            formData {
                var data: ByteArray
                var quality = 1f
                do {
                    data = resizedImage(Size(2000))
                        .asByteArray(quality)
                    quality -= 0.2f
                    Napier.i("quality $quality ${data.count()}")
                } while (data.count() > 250_000 && quality >= 0)
                appendInput(
                    key ?: "key",
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

    suspend fun putUser(user: User) = tryCheckAuth {
        client.client.put<Unit>("/user") {
            contentType(ContentType.Application.Json)
            body = user
        }
    }

    suspend fun setFavorite(favoriteSetter: FavoriteSetter) = tryCheckAuth {
        client.client.post<Unit>("/user/setFavorite") {
            contentType(ContentType.Application.Json)
            body = favoriteSetter
        }
    }

    suspend fun requestBecomeExpert() = tryCheckAuth {
        client.client.post<Unit>("/user/requestBecomeExpert")
    }

    suspend fun rate(ratingRequest: RatingRequest) = tryCheckAuth {
        client.client.post<Unit>("/user/rate") {
            contentType(ContentType.Application.Json)
            body = ratingRequest
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

private fun Throwable.toResponseException(): Throwable =
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

data class RedirectResponseException(val status: HttpStatusCode) : Exception() {
    constructor(exception: io.ktor.client.features.RedirectResponseException) : this(exception.response.status)
}

data class ClientRequestException(val status: HttpStatusCode) : Exception() {
    constructor(exception: io.ktor.client.features.ClientRequestException) : this(exception.response.status)
}

data class ServerResponseException(val status: HttpStatusCode) : Exception() {
    constructor(exception: io.ktor.client.features.ServerResponseException) : this(exception.response.status)
}

fun <T> Flow<T>.combineToNetworkConnectedState(networkManager: NetworkManager): Flow<T> =
    combine(networkManager.onConnectedFlow) { value, _ ->
        value
    }