package com.well.sharedMobile.puerh._featureProvider

import com.well.serverModels.User
import com.well.sharedMobile.networking.LoginNetworkManager
import com.well.sharedMobile.networking.NetworkManager
import com.well.sharedMobile.puerh._topLevel.*
import com.well.sharedMobile.puerh.call.webRtc.WebRtcManagerI
import com.well.sharedMobile.puerh.onlineUsers.OnlineUsersApiEffectHandler
import com.well.sharedMobile.puerh.onlineUsers.OnlineUsersFeature
import com.well.sharedMobile.puerh._topLevel.ContextHelper
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature.Eff
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature.Msg
import com.well.utils.*
import com.well.utils.atomic.AtomicLateInitRef
import com.well.utils.dataStore.authToken
import com.well.utils.dataStore.deviceUUID
import com.well.utils.permissionsHandler.PermissionsHandler
import com.well.utils.permissionsHandler.PermissionsHandler.Type.*
import com.well.utils.platform.Platform
import com.well.utils.platform.isDebug
import com.well.utils.puerh.*
import io.ktor.client.*
import kotlinx.coroutines.*

class FeatureProvider(
    val context: Context,
    internal val webRtcManagerGenerator: (List<String>, WebRtcManagerI.Listener) -> WebRtcManagerI,
) {
    private val coroutineContext = Dispatchers.Default
    private val sessionCloseableContainer = CloseableContainer()
    internal val platform = Platform(context)
    internal val permissionsHandler = PermissionsHandler(context.permissionsHandlerContext)
    internal val coroutineScope = CoroutineScope(coroutineContext)
    internal val contextHelper = ContextHelper(context)
    internal val networkManager = AtomicLateInitRef<NetworkManager>()
    internal val nonInitializedUserToken = AtomicLateInitRef<String>()
    internal val callCloseableContainer = CloseableContainer()

    private val effectInterpreter: ExecutorEffectsInterpreter<Eff, Msg> =
        interpreter@{ eff, listener ->
            when (eff) {
                is Eff.ShowAlert -> {
                    MainScope().launch {
                        contextHelper.showAlert(eff.alert)
                    }
                }
                is Eff.MyProfileEff -> handleMyProfileEff(eff.eff, listener)
                is Eff.OnlineUsersEff -> when (eff.eff) {
                    is OnlineUsersFeature.Eff.SelectedUser -> {
                        listener.invoke(
                            Msg.OpenUserProfile(
                                user = eff.eff.user,
                                isCurrent = networkManager.value.currentUser.value == eff.eff.user,
                            )
                        )
                    }
                    is OnlineUsersFeature.Eff.CallUser -> {
                        handleCall(eff.eff.user, listener)
                    }
                }
                is Eff.CallEff -> handleCallEff(eff.eff, listener)
//                is Eff.GotLogInToken -> {
//                    loggedIn(eff.token, listener)
//                    listener.invoke(Msg.LoggedIn)
//                }
                Eff.Initial -> {
                    val loginToken = platform.dataStore.authToken
                    if (loginToken != null) {
                        loggedIn(loginToken, listener)
                        listener.invoke(Msg.LoggedIn)
                    } else {
                        listener.invoke(Msg.OpenLoginScreen)
                        if (Platform.isDebug) {
                            while (true) {
                                try {
                                    val (token, user) = getTestLoginTokenAndUser()
                                    gotUser(user, token, listener)
                                    break
                                } catch (t: Throwable) {
                                    println("test login failed: $t")
                                    delay(5000)
                                }
                            }
                        }
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

    private suspend fun getTestLoginTokenAndUser(): Pair<String, User> {
        val deviceUUID = platform.dataStore.deviceUUID
            ?: run {
                randomUUIDString().also {
                    platform.dataStore.deviceUUID = it
                }
            }
        return LoginNetworkManager()
            .testLogin(deviceUUID)
    }

    private fun gotUser(
        user: User,
        token: String,
        listener: (Msg) -> Unit,
    ) {
        if (!user.initialized) {
            nonInitializedUserToken.value = token
            networkManager.value = NetworkManager(token)
            listener.invoke(Msg.OpenUserProfile(user, isCurrent = true))
        } else {
            loggedIn(token, listener)
        }
    }

    internal fun loggedIn(
        token: String,
        listener: (Msg) -> Unit,
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
        platform.dataStore.authToken = token
        listener.invoke(Msg.LoggedIn)

//        coroutineScope.launch {
//            val image = networkManager.value.uploadImage(contextHelper.pickSystemImage())
//            println("new image $image")
//        }
    }
}