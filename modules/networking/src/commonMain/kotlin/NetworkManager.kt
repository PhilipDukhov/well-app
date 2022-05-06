package com.well.modules.networking

import com.well.modules.atomic.AtomicRef
import com.well.modules.atomic.Closeable
import com.well.modules.atomic.CloseableContainer
import com.well.modules.models.Availability
import com.well.modules.models.BookingAvailabilitiesListByDay
import com.well.modules.models.BookingAvailability
import com.well.modules.models.ConnectionStatus.Connected
import com.well.modules.models.ConnectionStatus.Connecting
import com.well.modules.models.ConnectionStatus.Disconnected
import com.well.modules.models.DeviceId
import com.well.modules.models.FavoriteSetter
import com.well.modules.models.NetworkConstants
import com.well.modules.models.RatingRequest
import com.well.modules.models.User
import com.well.modules.models.WebSocketMsg
import com.well.modules.networking.webSocketManager.WebSocketClient
import com.well.modules.utils.kotlinUtils.tryF
import com.well.modules.utils.viewUtils.platform.Platform
import com.well.modules.utils.viewUtils.platform.fileSystem
import com.well.modules.utils.viewUtils.platform.isLocalServer
import io.github.aakira.napier.Napier
import io.ktor.client.call.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okio.Path

class NetworkManager(
    token: String,
    deviceId: DeviceId,
    startWebSocket: Boolean,
    private val services: Services,
) : CloseableContainer() {
    data class Services(
        val onUnauthorized: () -> Unit,
        val onUpdateNeeded: () -> Unit,
    )

    private val clientWrapper = WebSocketClient(
        NetworkConstants.current(Platform.isLocalServer).let { constants ->
            WebSocketClient.Config(
                host = constants.host,
                port = constants.port,
                webSocketProtocol = constants.webSocketProtocol,
                bearerToken = token,
            )
        }
    )
    private val client get() = clientWrapper.client

    private val webSocketScope = CoroutineScope(Dispatchers.Default)

    private var webSocketSession by AtomicRef<DefaultClientWebSocketSession?>(null)

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
                    client.ws("mainWebSocket/${deviceId}") {
                        webSocketSession = this
                        _connectionStatus.value = Connected
                        Napier.i("Connected")
                        for (frame in incoming) {
                            when (frame) {
                                is Frame.Text -> {
                                    val string = frame.readText()
                                    Napier.i("websocket msg: $string")
                                    val msg = Json.decodeFromString(
                                        WebSocketMsg.serializer(),
                                        string
                                    )
                                    _webSocketMsgSharedFlow.emit(msg)
                                }
                                else -> {
                                    Napier.e("unexpected web socket frame: $frame")
                                }
                            }

                        }
                    }
                } catch (e: SerializationException) {
                    Napier.e("web socket serialization error", e)
                    services.onUpdateNeeded()
                    break
                } catch (e: Exception) {
                    if (handleUnauthorized(e)) break
                    Napier.e("web socket connection error", e)
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

    override fun close() {
        super.close()
        clientWrapper.client.close()
    }

    suspend fun sendFront(msg: WebSocketMsg.Front) = send(msg as WebSocketMsg)

    suspend fun sendCall(msg: WebSocketMsg.Call) = send(msg as WebSocketMsg)

    private suspend fun send(msg: WebSocketMsg) {
        try {
            Napier.i("send WebSocketMsg: $msg")
            if (webSocketSession == null) {
                Napier.e("webSocketSession is null $msg")
            }
            webSocketSession?.send(
                Json.encodeToString(
                    WebSocketMsg.serializer(),
                    msg
                )
            )
        } catch (e: Exception) {
            Napier.e("failed to send $msg", e)
        }
    }

    suspend fun uploadProfilePicture(
        data: ByteArray,
    ): String = tryCheckAuth {
        client.post("user/uploadProfilePicture") {
            multipartFormBody {
                appendByteArrayInput(data)
            }
        }.body()
    }

    suspend fun uploadMessagePicture(
        data: ByteArray,
    ): String = tryCheckAuth {
        client.post("uploadMessageMedia") {
            multipartFormBody {
                appendByteArrayInput(data)
            }
        }.body()
    }

    suspend fun sendTechSupportMessage(
        message: String,
        path: Path? = null,
    ): String = tryCheckAuth {
        client.post("techSupportMessage") {
            multipartFormBody {
                if (path != null) {
                    val data = Platform.fileSystem.read(path) {
                        readByteArray()
                    }
                    appendByteArrayInput(data, filename = path.name)
                }
                append("message", message)
            }
        }.body()
    }

    suspend fun putUser(user: User): Unit = tryCheckAuth {
        client.put("user") {
            contentType(ContentType.Application.Json)
            setBody(user)
        }
    }

    suspend fun setFavorite(favoriteSetter: FavoriteSetter): Unit = tryCheckAuth {
        client.post("user/setFavorite") {
            contentType(ContentType.Application.Json)
            setBody(favoriteSetter)
        }
    }

    suspend fun requestBecomeExpert(): Unit = tryCheckAuth {
        client.post("user/requestBecomeExpert")
    }

    suspend fun rate(ratingRequest: RatingRequest): Unit = tryCheckAuth {
        client.post("user/rate") {
            contentType(ContentType.Application.Json)
            setBody(ratingRequest)
        }
    }

    private suspend fun <R> tryCheckAuth(
        block: suspend () -> R,
    ) = tryF(::handleUnauthorized, block = block)

    private fun handleUnauthorized(e: Exception): Boolean {
        @Suppress("NAME_SHADOWING")
        when (val e = e.toResponseException()) {
            is ClientRequestException -> {
                if (e.status == HttpStatusCode.Unauthorized) {
                    services.onUnauthorized()
                    return true
                }
            }
        }
        return false
    }

    suspend fun listCurrentUserAvailabilities(): List<Availability> =
        client.get("availabilities/listCurrent").body()

    suspend fun removeAvailability(availabilityId: Availability.Id) {
        client.delete("availabilities/${availabilityId.value}")
    }

    suspend fun putAvailability(availability: Availability): Availability =
        client.put("availabilities") {
            contentType(ContentType.Application.Json)
            setBody(availability)
        }.body()

    suspend fun userHasAvailableAvailabilities(userId: User.Id): Boolean =
        client.get("availabilities/userHasAvailable/${userId.value}").body()

    suspend fun book(availability: BookingAvailability) {
        client.post("availabilities/book") {
            contentType(ContentType.Application.Json)
            setBody(availability)
        }
    }

    suspend fun getAvailabilities(userId: User.Id): BookingAvailabilitiesListByDay =
        client.get("availabilities/listByUser/${userId.value}").body()

    suspend fun deleteProfile() {
        client.delete("user")
    }
}