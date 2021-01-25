package com.well.sharedMobile.puerh.featureProvider

import com.well.serverModels.Date
import com.well.serverModels.User
import com.well.serverModels.WebSocketMessage
import com.well.sharedMobile.networking.LoginNetworkManager
import com.well.sharedMobile.networking.webSocketManager.NetworkManager
import com.well.sharedMobile.puerh.call.CallFeature
import com.well.sharedMobile.puerh.call.webRtc.WebRtcEffectHandler
import com.well.sharedMobile.puerh.call.webRtc.WebRtcManagerI
import com.well.sharedMobile.puerh.call.imageSharing.ImageSharingFeature
import com.well.sharedMobile.puerh.onlineUsers.OnlineUsersApiEffectHandler
import com.well.sharedMobile.puerh.onlineUsers.OnlineUsersFeature
import com.well.sharedMobile.puerh.topLevel.Alert
import com.well.sharedMobile.puerh.topLevel.Alert.CameraOrMicDenied
import com.well.sharedMobile.puerh.topLevel.ContextHelper
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature
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
import com.well.utils.platform.isDebug
import io.ktor.client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class FeatureProvider(
    val context: Context,
    private val webRtcManagerGenerator: (List<String>, WebRtcManagerI.Listener) -> WebRtcManagerI,
) {
    private val platform = Platform(context)
    private val permissionsHandler = PermissionsHandler(context.permissionsHandlerContext)
    private val coroutineContext = Dispatchers.Default
    private val coroutineScope = CoroutineScope(coroutineContext)
    private val contextHelper = ContextHelper(context)
    private val sessionCloseableContainer = object : CloseableContainer() {}
    private val networkManager = AtomicLateInitRef<NetworkManager>()
    private val callEventHandlerCloseable = AtomicCloseableRef()

    private val effectInterpreter: ExecutorEffectsInterpreter<Eff, Msg> =
        interpreter@{ eff, listener ->
            when (eff) {
                is Eff.ShowAlert ->
                    contextHelper.showAlert(eff.alert)
                is Eff.OnlineUsersEff -> when (eff.eff) {
                    is OnlineUsersFeature.Eff.CallUser -> {
                        handleCall(eff.eff.user, listener)
                    }
                }
                is Eff.CallEff -> when (eff.eff) {
                    is CallFeature.Eff.NotifyDeviceStateChanged,
                    is CallFeature.Eff.SyncLocalDeviceState,
                    -> Unit
                    is CallFeature.Eff.Initiate, is CallFeature.Eff.Accept -> {
                        callEventHandlerCloseable.value =
                            createWebRtcManagerHandler(eff.eff).freeze()
                    }
                    is CallFeature.Eff.End -> {
                        println("WebRtcEffectHandler close $this")
                        networkManager.value.send(
                            WebSocketMessage.EndCall(WebSocketMessage.EndCall.Reason.Decline)
                        )
                        endCall(listener)
                    }
                    CallFeature.Eff.StartImageSharing -> {
                        listener(Msg.StartImageSharing(ImageSharingFeature.State.Role.Editor))
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
                is Eff.ImageSharingEff -> when (eff.eff) {
                    is ImageSharingFeature.Eff.NotifyViewSizeUpdate,
                    is ImageSharingFeature.Eff.UploadImage,
                    ImageSharingFeature.Eff.SendInit,
                    is ImageSharingFeature.Eff.UploadPaths,
                    -> Unit

                    ImageSharingFeature.Eff.RequestImageUpdate -> {
                        println("${Date()} handle RequestImageUpdate")
                        CoroutineScope(Dispatchers.Default).launch {
                            val msg = try {
                                ImageSharingFeature.Msg.LocalUpdateImage(
//                                if (Platform.isDebug)
//                                    networkManager.value.downloadTestImage()
//                                else
                                    contextHelper.pickSystemImage()
                                )
                            } catch (t: Throwable) {
                                println("LocalUpdateImage failed $t")
                                ImageSharingFeature.Msg.ImageUpdateCancelled
                            }
                            listener(Msg.ImageSharingMsg(msg))
                        }
                    }
                    ImageSharingFeature.Eff.Close -> {
                        listener(Msg.StopImageSharing)
                    }
                }
                Eff.SystemBack -> {
                    context.systemBack()
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
        return LoginNetworkManager()
            .testLogin(deviceUUID)
            .also {
                platform.dataStore.loginToken = it
            }
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
    ) = handleCallPermissions()?.also {
        listener(Msg.ShowAlert(it.first.alert))
    } ?: listener(Msg.StartCall(user))

    private fun endCall(
        listener: (Msg) -> Unit,
    ) {
        println("end call ${callEventHandlerCloseable.value}")
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
                        initiateEffect?.let { handleEffect(Eff.CallEff(it)) }
                    }
            )
    }

    private val PermissionsHandler.Type.alert: Alert
        get() = when (this) {
            Camera, Microphone -> CameraOrMicDenied
        }
}