package com.well.sharedMobile.puerh._featureProvider

import com.well.sharedMobile.networking.NetworkManager
import com.well.sharedMobile.puerh._topLevel.*
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature.Eff
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature.Msg
import com.well.sharedMobile.puerh.call.webRtc.WebRtcManagerI
import com.well.sharedMobile.puerh.login.LoginFeature
import com.well.sharedMobile.puerh.login.SocialNetwork
import com.well.sharedMobile.puerh.login.SocialNetworkService
import com.well.sharedMobile.puerh.login.credentialProviders.CredentialProvider
import com.well.sharedMobile.puerh.onlineUsers.OnlineUsersFeature
import com.well.modules.utils.*
import com.well.modules.atomic.AtomicLateInitRef
import com.well.modules.atomic.CloseableContainer
import com.well.modules.atomic.freeze
import com.well.modules.napier.Napier
import com.well.modules.utils.base.puerh.SyncFeature
import com.well.modules.utils.dataStore.authToken
import com.well.modules.utils.permissionsHandler.PermissionsHandler
import com.well.modules.utils.permissionsHandler.PermissionsHandler.Type.*
import com.well.modules.utils.platform.Platform
import com.well.modules.utils.platform.isDebug
import com.well.modules.utils.puerh.*
import io.ktor.client.*
import kotlinx.coroutines.*

class FeatureProvider(
    val context: Context,
    internal val webRtcManagerGenerator: (List<String>, WebRtcManagerI.Listener) -> WebRtcManagerI,
    providerGenerator: (SocialNetwork, Context) -> CredentialProvider,
) {
    private val coroutineContext = Dispatchers.Default
    internal val sessionCloseableContainer = CloseableContainer()
    internal val socialNetworkService = SocialNetworkService {
        providerGenerator(it, context)
    }
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
                    if (eff.alert is Alert.Throwable) {
                        Napier.e("", eff.alert.throwable)
                        if (!Platform.isDebug) {
                            println("alert! ${eff.alert.throwable}")
                        }
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
                    OnlineUsersFeature.Eff.Logout -> {
                        logOut(listener)
                    }
                }
                is Eff.CallEff -> handleCallEff(eff.eff, listener)
                Eff.Initial -> {
                    val loginToken = platform.dataStore.authToken
                    if (loginToken != null) {
                        loggedIn(loginToken, listener)
                        listener.invoke(Msg.LoggedIn)
                    } else {
                        listener.invoke(Msg.OpenLoginScreen)
                    }
                }
                Eff.SystemBack -> {
                    context.systemBack()
                }
                is Eff.LoginEff -> {
                    when (eff.eff) {
                        is LoginFeature.Eff.Login -> {
                            socialNetworkLogin(eff.eff.socialNetwork, listener)
                        }
                    }
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
}