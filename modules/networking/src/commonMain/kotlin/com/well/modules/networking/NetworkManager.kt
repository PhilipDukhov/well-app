package com.well.modules.networking

import com.well.modules.atomic.AtomicRef
import com.well.modules.atomic.Closeable
import com.well.modules.atomic.CloseableContainer
import com.well.modules.models.Availability
import com.well.modules.models.AvailabilityId
import com.well.modules.models.ConnectionStatus.Connected
import com.well.modules.models.ConnectionStatus.Connecting
import com.well.modules.models.ConnectionStatus.Disconnected
import com.well.modules.models.FavoriteSetter
import com.well.modules.models.NetworkConstants
import com.well.modules.models.RatingRequest
import com.well.modules.models.User
import com.well.modules.models.UserId
import com.well.modules.models.WebSocketMsg
import com.well.modules.networking.webSocketManager.WebSocketClient
import com.well.modules.networking.webSocketManager.WebSocketSession
import com.well.modules.networking.webSocketManager.ws
import com.well.modules.utils.kotlinUtils.tryF
import com.well.modules.utils.viewUtils.platform.Platform
import com.well.modules.utils.viewUtils.platform.isDebug
import io.github.aakira.napier.Napier
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.util.*
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class NetworkManager(
    token: String,
    startWebSocket: Boolean,
    private val unauthorizedHandler: () -> Unit,
) : CloseableContainer() {
    private val clientWrapper = WebSocketClient(
        NetworkConstants.current(Platform.isDebug).let { constants ->
            WebSocketClient.Config(
                host = constants.host,
                port = constants.port,
                webSocketProtocol = constants.webSocketProtocol,
                bearerToken = token,
            )
        }
    )
    private val client = clientWrapper.client

    private val webSocketScope = CoroutineScope(Dispatchers.Default)

    private var webSocketSession by AtomicRef<WebSocketSession?>(null)

    private val _webSocketMsgSharedFlow = MutableSharedFlow<WebSocketMsg>()
    val webSocketMsgSharedFlow: Flow<WebSocketMsg> = _webSocketMsgSharedFlow

    private val _connectionStatus = MutableStateFlow(Disconnected)

    val connectionStatusFlow = _connectionStatus.asStateFlow()
    val isConnectedFlow = connectionStatusFlow.map { it == Connected }.distinctUntilChanged()
    val onConnectedFlow = isConnectedFlow.filter { it }.map { }

    init {
        webSocketScope.launch {
            while (startWebSocket) {
                try {
                    _connectionStatus.value = Connecting
                    Napier.i("Connecting")
                    clientWrapper.ws("/mainWebSocket") {
                        webSocketSession = this
                        _connectionStatus.value = Connected
                        Napier.i("Connected")
                        for (string in incoming) {
                            Napier.i("websocket msg: $string")
                            val msg = Json.decodeFromString(
                                WebSocketMsg.serializer(),
                                string
                            )
                            _webSocketMsgSharedFlow.emit(msg)
                        }
                    }
                } catch (t: Throwable) {
                    if (handleUnauthorized(t)) break
                    Napier.e("web socket connection error", t)
                } finally {
                    val wasConnected = _connectionStatus.value == Connected
                    _connectionStatus.value = Disconnected
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

    suspend fun sendFront(msg: WebSocketMsg.Front) = send(msg as WebSocketMsg)

    suspend fun sendCall(msg: WebSocketMsg.Call) = send(msg as WebSocketMsg)

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
        data: ByteArray,
    ) = tryCheckAuth {
        client.post<String>("/user/uploadProfilePicture") {
            body = data.toMultiPartFormDataContent(userId.toString())
        }
    }

    suspend fun uploadMessagePicture(
        data: ByteArray,
    ) = tryCheckAuth {
        client.post<String>("uploadMessageMedia") {
            body = data.toMultiPartFormDataContent()
        }
    }

    private fun ByteArray.toMultiPartFormDataContent(key: String? = null) =
        MultiPartFormDataContent(
            formData {
                appendInput(
                    key ?: "key",
                    Headers.build {
                        @OptIn(InternalAPI::class)
                        append(
                            HttpHeaders.ContentDisposition,
                            "filename=uploadUserProfile.jpg"
                        )
                    },
                    size.toLong()
                ) {
                    buildPacket { writeFully(this@toMultiPartFormDataContent) }
                }
            }
        )

    suspend fun putUser(user: User) = tryCheckAuth {
        client.put<Unit>("/user") {
            contentType(ContentType.Application.Json)
            body = user
        }
    }

    suspend fun setFavorite(favoriteSetter: FavoriteSetter) = tryCheckAuth {
        client.post<Unit>("/user/setFavorite") {
            contentType(ContentType.Application.Json)
            body = favoriteSetter
        }
    }

    suspend fun requestBecomeExpert() = tryCheckAuth {
        client.post<Unit>("/user/requestBecomeExpert")
    }

    suspend fun rate(ratingRequest: RatingRequest) = tryCheckAuth {
        client.post<Unit>("/user/rate") {
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

    suspend fun addAvailability(availability: Availability) {

    }
    suspend fun removeAvailability(availabilityId: AvailabilityId) {

    }
    suspend fun updateAvailability(availability: Availability) {

    }
    suspend fun book(availability: Availability) {

    }
    suspend fun getAvailabilities(userId: UserId): List<Availability> =
        listOf()
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