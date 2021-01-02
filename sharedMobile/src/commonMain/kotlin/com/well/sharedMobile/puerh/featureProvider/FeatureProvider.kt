package com.well.sharedMobile.puerh.featureProvider

import com.well.serverModels.User
import com.well.serverModels.WebSocketMessage
import com.well.sharedMobile.networking.LoginNetworkManager
import com.well.sharedMobile.networking.webSocketManager.NetworkManager
import com.well.sharedMobile.puerh.AlertHelper
import com.well.sharedMobile.puerh.call.CallFeature
import com.well.sharedMobile.puerh.call.WebRtcEffectHandler
import com.well.sharedMobile.puerh.call.WebRtcManagerI
import com.well.sharedMobile.puerh.onlineUsers.OnlineUsersApiEffectHandler
import com.well.sharedMobile.puerh.onlineUsers.OnlineUsersFeature
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature.Alert.CameraOrMicDenied
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature.Eff
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature.Msg
import com.well.utils.*
import com.well.utils.atomic.AtomicCloseableRef
import com.well.utils.atomic.AtomicLateInitRef
import com.well.utils.permissionsHandler.PermissionsHandler
import com.well.utils.permissionsHandler.PermissionsHandler.Result.Authorized
import com.well.utils.permissionsHandler.PermissionsHandler.Type.*
import com.well.utils.permissionsHandler.requestPermissions
import com.well.utils.platform.Platform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FeatureProvider(
    val context: Context,
    private val webRtcManagerGenerator: (List<String>, WebRtcManagerI.Listener) -> WebRtcManagerI,
) {
    private val platform = Platform(context)
    private val permissionsHandler = PermissionsHandler(context.permissionsHandlerContext)
    private val coroutineContext = Dispatchers.Default
    private val coroutineScope = CoroutineScope(coroutineContext)
    private val alertHelper = AlertHelper(context)
    private val sessionCloseableContainer = object : CloseableContainer() {}
    private var networkManager = AtomicLateInitRef<NetworkManager>()
    private var callEventHandlerCloseable = AtomicCloseableRef()

    private val effectInterpreter: ExecutorEffectsInterpreter<Eff, Msg> =
        interpreter@{ eff, listener ->
            when (eff) {
                is Eff.ShowAlert ->
                    alertHelper.showAlert(eff.alert)
                is Eff.OnlineUsersEff -> when (eff.eff) {
                    is OnlineUsersFeature.Eff.CallUser -> {
                        handleCall(eff.eff.user, listener)
                    }
                }
                is Eff.CallEff -> when (eff.eff) {
                    is CallFeature.Eff.Initiate, is CallFeature.Eff.Accept -> {
                        callEventHandlerCloseable.value =
                            createWebRtcManagerHandler(eff.eff).freeze()
                    }
                    is CallFeature.Eff.End -> {
                        networkManager.value.send(
                            WebSocketMessage.EndCall(WebSocketMessage.EndCall.Reason.Busy)
                        )
                        endCall(listener)
                    }
                }
                is Eff.GotLogInToken -> {
                    loggedIn(eff.token, listener)
                    listener.invoke(Msg.LoggedIn)
                }
                Eff.TestLogin -> {
                    listener.invoke(Msg.LoggedIn)
                    loggedIn(getTestLoginToken(), listener)
                }
            }
        }

    val feature = SyncFeature(
        TopLevelFeature.initialState(),
        TopLevelFeature.initialEffects(),
        TopLevelFeature::reducer,
        coroutineContext
    )

    init {
        // preventing iOS InvalidMutabilityException
        val handler = ExecutorEffectHandler(
            effectInterpreter,
            coroutineScope,
        )
        feature.wrapWithEffectHandler(
            handler
        )
        handler.freeze()
    }

    private suspend fun getTestLoginToken(): String {
        val loginToken = platform.dataStore.loginToken
        if (loginToken != null) return loginToken
        val deviceUUID = platform.dataStore.deviceUUID
            ?: run {
                randomUUIDString().also {
                    platform.dataStore.deviceUUID = it
                }
            }
        return LoginNetworkManager().testLogin(deviceUUID)
    }

    private fun loggedIn(
        token: String,
        listener: (Msg) -> Unit
    ) {
        sessionCloseableContainer.close()
        networkManager.value = NetworkManager(token)
        val effectHandler: EffectHandler<Eff, Msg> =
            OnlineUsersApiEffectHandler(
                networkManager.value,
                coroutineScope,
            ).adapt(
                effAdapter = { (it as? Eff.OnlineUsersEff)?.eff },
                msgAdapter = { Msg.OnlineUsersMsg(it) }
            )
        listOf(
            networkManager.value
                .addListener(createWebSocketMessageHandler(listener)),
            effectHandler,
            networkManager.value,
        ).forEach(sessionCloseableContainer::addCloseableChild)
        feature.wrapWithEffectHandler(effectHandler)
    }

    private fun createWebSocketMessageHandler(listener: (Msg) -> Unit): (WebSocketMessage) -> Unit =
        { msg ->
            when (msg) {
                is WebSocketMessage.IncomingCall -> {
                    listener.invoke(Msg.IncomingCall(msg))
                    coroutineScope.launch {
                        handleCallPermissions()?.also {
                            listener(Msg.EndCall)
                            listener(Msg.ShowAlert(it.first.alert))
                        }
                    }
                }
                is WebSocketMessage.EndCall -> {
                    endCall(listener)
                }
                else -> Unit
            }
        }

    private suspend fun handleCall(
        user: User,
        listener: (Msg) -> Unit
    ) =
        handleCallPermissions()?.also {
            listener(Msg.ShowAlert(it.first.alert))
        } ?: listener(Msg.StartCall(user))

    private fun endCall(
        listener: (Msg) -> Unit,
    ) {
        listener.invoke(Msg.EndCall)
        callEventHandlerCloseable.value = null
    }

    private suspend fun handleCallPermissions() =
        permissionsHandler
            .requestPermissions(
                Camera,
                Microphone,
            )
            .firstOrNull {
                it.second != Authorized
            }

    private fun createWebRtcManagerHandler(
        initiateEffect: CallFeature.Eff? = null,
    ) = CloseableFuture(coroutineScope) {
        feature
            .addEffectHandler(
                WebRtcEffectHandler(
                    networkManager.value,
                    webRtcManagerGenerator,
                    coroutineScope,
                )
                    .apply {
                        initiateEffect?.let(::handleEffect)
                    }
                    .adapt(
                        effAdapter = { (it as? Eff.CallEff)?.eff },
                        msgAdapter = { Msg.CallMsg(it) }
                    )
            )
    }

    private val PermissionsHandler.Type.alert: TopLevelFeature.Alert
        get() = when (this) {
            Camera, Microphone -> CameraOrMicDenied
        }
}