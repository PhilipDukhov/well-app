package com.well.sharedMobile.networking

import com.well.modules.models.*
import com.well.sharedMobile.networking.NetworkManager.Status.*
import com.well.sharedMobile.networking.webSocketManager.WebSocketClient
import com.well.sharedMobile.networking.webSocketManager.WebSocketMessageListener
import com.well.sharedMobile.networking.webSocketManager.WebSocketSession
import com.well.sharedMobile.networking.webSocketManager.ws
import com.well.sharedMobile.puerh.call.resizedImage
import com.well.sharedMobile.utils.ImageContainer
import com.well.modules.atomic.Closeable
import com.well.modules.atomic.CloseableContainer
import com.well.modules.utils.MutableStateFlow
import com.well.modules.atomic.*
import com.well.modules.napier.Napier
import com.well.modules.utils.tryF
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
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
    private val listeners = AtomicMutableList<WebSocketMessageListener>()

    private val _state = MutableStateFlow(Disconnected)
    private val _currentUser = MutableStateFlow<User?>()

    val state = _state.asStateFlow()
    val currentUser = _currentUser.asStateFlow()

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
                            val msg = Json.decodeFromString(
                                WebSocketMsg.serializer(),
                                string
                            )
                            when (msg) {
                                is WebSocketMsg.CurrentUser ->
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

    fun addListener(listener: WebSocketMessageListener): Closeable =
        listeners.addListenerAndMakeCloseable(listener)
            .also(::addCloseableChild)

    suspend fun send(msg: WebSocketMsg) {
        try {
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

    suspend fun uploadImage(
        userId: UserId,
        image: ImageContainer
    ) = tryCheckAuth {
        client.client.post<String>("/user/uploadProfileImage") {
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
            body = user
        }
    }

    suspend fun filteredUsersList(filter: UsersFilter) = tryCheckAuth {
        client.client.post<List<User>>("/user/filteredList") {
            body = filter
        }
    }

    suspend fun setFavorite(favoriteSetter: FavoriteSetter) = tryCheckAuth {
        client.client.post<Unit>("/user/setFavorite") {
            body = favoriteSetter
        }
    }

    suspend fun requestBecomeExpert() = tryCheckAuth {
        client.client.post<Unit>("/user/requestBecomeExpert")
    }

    suspend fun rate(ratingRequest: RatingRequest) = tryCheckAuth {
        client.client.post<Unit>("/user/rate") {
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