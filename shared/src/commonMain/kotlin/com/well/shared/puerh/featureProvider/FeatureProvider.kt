package com.well.shared.puerh.featureProvider

import com.well.serverModels.User
import com.well.shared.puerh.WebSocketManager
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

class FeatureProvider(val context: Context, testDevice: TestDevice) {
    private val permissionsHandler = PermissionsHandler(context.permissionsHandlerContext)
    private val coroutineContext = Dispatchers.Default
    private val coroutineScope = CoroutineScope(coroutineContext)
    private lateinit var webSocketManager: WebSocketManager
    private val sessionCloseableContainer = object : CloseableContainer() {}

    private val callEffectInterpreter: ExecutorEffectsInterpreter<Eff, Msg> =
        interpreter@ { eff, listener ->
            if (eff is Eff.OnlineUsersEff && eff.eff is OnlineUsersFeature.Eff.CallUser) {
                handleCall(eff.eff.user, listener)
            }
        }

    val feature = SyncFeature(
        TopLevelFeature.initialState(),
        TopLevelFeature.initialEffects(testDevice),
        TopLevelFeature::reducer,
        coroutineContext
    ).wrapWithEffectHandler(
        ExecutorEffectHandler(
            callEffectInterpreter,
            coroutineScope,
        )
    ).apply {
        wrapWithEffectHandler(
            ExecutorEffectHandler({ eff, listener ->
                when (eff) {
                    is Eff.GotLogInToken -> login(eff.token, listener)
                    else -> Unit
                }
            }, coroutineScope)
        )
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
        listOf(effectHandler, webSocketManager)
            .forEach(sessionCloseableContainer::addCloseableChild)
        feature.wrapWithEffectHandler(effectHandler)
        listener.invoke(Msg.LoggedIn)
    }

    private suspend fun handleCall(user: User, listener: (Msg) -> Unit) =
        permissionsHandler
            .requestPermissions(
                Camera,
                Microphone,
            ).firstOrNull {
                it.second != Authorized
            }?.also {
                listener(Msg.ShowAlert(it.first.alert))
            } ?: listener(Msg.Call(user))

    private val PermissionsHandler.Type.alert: TopLevelFeature.Alert
        get() = when (this) {
            Camera, Microphone -> CameraOrMicDenied
        }
}