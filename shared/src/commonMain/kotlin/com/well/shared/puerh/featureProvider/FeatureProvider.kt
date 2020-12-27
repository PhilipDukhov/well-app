package com.well.shared.puerh.featureProvider

import com.well.serverModels.User
import com.well.serverModels.WebSocketMessage
import com.well.shared.puerh.AlertHelper
import com.well.shared.puerh.WebSocketManager
import com.well.shared.puerh.call.CallFeature
import com.well.shared.puerh.onlineUsers.OnlineUsersApiEffectHandler
import com.well.shared.puerh.onlineUsers.OnlineUsersFeature
import com.well.shared.puerh.topLevel.TestDevice
import com.well.shared.puerh.topLevel.TopLevelFeature
import com.well.shared.puerh.topLevel.TopLevelFeature.Eff
import com.well.shared.puerh.topLevel.TopLevelFeature.Msg
import com.well.shared.puerh.topLevel.TopLevelFeature.Alert.CameraOrMicDenied
import com.well.utils.*
import com.well.utils.permissionsHandler.PermissionsHandler
import com.well.utils.permissionsHandler.PermissionsHandler.Result.Authorized
import com.well.utils.permissionsHandler.PermissionsHandler.Type.*
import com.well.utils.permissionsHandler.requestPermissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FeatureProvider(
    val context: Context,
    testDevice: TestDevice,
    val webRTCManagerGenerator: (WebSocketManager) -> EffectHandler<CallFeature.Eff, CallFeature.Msg>,
) {
    private val permissionsHandler = PermissionsHandler(context.permissionsHandlerContext)
    private val coroutineContext = Dispatchers.Default
    private val coroutineScope = CoroutineScope(coroutineContext)
    lateinit var webSocketManager: WebSocketManager
    private val alertHelper = AlertHelper(context)
    private val sessionCloseableContainer = object : CloseableContainer() {}
    private var callEventHandlerCloseable: Closeable? = null
        set(value) {
            field?.close()
            field = value
        }

    private val effectInterpreter: ExecutorEffectsInterpreter<Eff, Msg> =
        interpreter@ { eff, listener ->
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
                        callEventHandlerCloseable = createWebRtcManagerHandler(eff.eff)
                    }
                    is CallFeature.Eff.End -> {
                        callEventHandlerCloseable = null
                        listener.invoke(Msg.EndCall)
                    }
                }
                is Eff.GotLogInToken -> login(eff.token, listener)
            }
        }

    private fun login(token: String, listener: (Msg) -> Unit) {
        sessionCloseableContainer.close()
        webSocketManager = WebSocketManager(token)
        val effectHandler: EffectHandler<Eff, Msg> =
            OnlineUsersApiEffectHandler(
                webSocketManager,
                coroutineScope,
            ).adapt(
                effAdapter = { (it as? Eff.OnlineUsersEff)?.eff },
                msgAdapter = { Msg.OnlineUsersMsg(it) }
            )
        coroutineScope.launch {
            listOf(
                webSocketManager.addListener(createWebSocketMessageHandler(listener)),
                effectHandler,
                webSocketManager,
            ).forEach(sessionCloseableContainer::addCloseableChild)
            feature.wrapWithEffectHandler(effectHandler)
            listener.invoke(Msg.LoggedIn)
        }
    }

    private fun createWebSocketMessageHandler(listener: (Msg) -> Unit): (WebSocketMessage) -> Unit =
        { msg -> when (msg) {
            is WebSocketMessage.IncomingCall -> {
                listener.invoke(Msg.IncomingCall(msg))
                coroutineScope.launch {
                    handleCallPermissions()?.also {
                        listener(Msg.EndCall)
                        listener(Msg.ShowAlert(it.first.alert))
                    }
                }
            }
            is WebSocketMessage.Candidate -> {
                println("$msg $callEventHandlerCloseable")
            }
            else -> Unit
        } }

    private suspend fun handleCall(user: User, listener: (Msg) -> Unit) =
        handleCallPermissions()?.also {
            listener(Msg.ShowAlert(it.first.alert))
        } ?: listener(Msg.StartCall(user))

    private suspend fun handleCallPermissions() =
        permissionsHandler
            .requestPermissions(
                Camera,
                Microphone,
            ).firstOrNull {
                it.second != Authorized
            }

    private fun createWebRtcManagerHandler(
        initiateEffect: CallFeature.Eff? = null,
    ) =
        feature
            .addEffectHandler(
                webRTCManagerGenerator(webSocketManager)
                    .apply {
                        initiateEffect?.let(::handleEffect)
                    }.adapt(
                        effAdapter = { (it as? Eff.CallEff)?.eff },
                        msgAdapter = { Msg.CallMsg(it) }
                    )
            )

    private val PermissionsHandler.Type.alert: TopLevelFeature.Alert
        get() = when (this) {
            Camera, Microphone -> CameraOrMicDenied
        }

    val feature = SyncFeature(
        TopLevelFeature.initialState(),
        TopLevelFeature.initialEffects(testDevice),
        TopLevelFeature::reducer,
        coroutineContext
    ).wrapWithEffectHandler(
        ExecutorEffectHandler(
            effectInterpreter,
            coroutineScope,
        )
    )
}