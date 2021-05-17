package com.well.sharedMobile.puerh._featureProvider

import com.well.modules.models.AuthResponse
import com.well.modules.models.User
import com.well.sharedMobile.networking.NetworkManager
import com.well.sharedMobile.puerh._topLevel.Alert
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature
import com.well.sharedMobile.puerh.login.LoginFeature
import com.well.sharedMobile.puerh.login.SocialNetwork
import com.well.sharedMobile.puerh.experts.ExpertsApiEffectHandler
import com.well.modules.utils.dataStore.authToken
import com.well.modules.utils.dataStore.deviceUUID
import com.well.modules.utils.puerh.EffectHandler
import com.well.modules.utils.puerh.adapt
import com.well.modules.utils.puerh.wrapWithEffectHandler
import com.well.modules.utils.randomUUIDString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

suspend fun FeatureProvider.socialNetworkLogin(
    socialNetwork: SocialNetwork,
    listener: (TopLevelFeature.Msg) -> Unit
) {
    try {
        gotAuthResponse(socialNetworkService.login(socialNetwork), listener)
    } catch (t: Throwable) {
        if (t !is CancellationException && t.message?.contains("com.well.modules.utils error 0") != true) {
            listener.invoke(TopLevelFeature.Msg.ShowAlert(Alert.Throwable(t)))
        }
    } finally {
        listener.invoke(TopLevelFeature.Msg.LoginMsg(LoginFeature.Msg.LoginAttemptFinished))
    }
}

fun FeatureProvider.gotAuthResponse(
    authResponse: AuthResponse,
    listener: (TopLevelFeature.Msg) -> Unit,
) {
    if (!authResponse.user.initialized) {
        nonInitializedUserToken.value = authResponse.token
        sessionCloseableContainer.close()
        networkManager.value = NetworkManager(
            authResponse.token,
            startWebSocket = false,
            unauthorizedHandler = {
                sessionCloseableContainer.close()
            })
        sessionCloseableContainer.addCloseableChild(networkManager.value)
        listener.invoke(TopLevelFeature.Msg.OpenUserProfile(authResponse.user, isCurrent = true))
    } else {
        loggedIn(authResponse.token, listener)
    }
}

fun FeatureProvider.loggedIn(
    token: String,
    listener: (TopLevelFeature.Msg) -> Unit,
) {
    sessionCloseableContainer.close()
    networkManager.value = NetworkManager(token, startWebSocket = true, unauthorizedHandler = {
        logOut(listener)
    })
    val effectHandler: EffectHandler<TopLevelFeature.Eff, TopLevelFeature.Msg> =
        ExpertsApiEffectHandler(
            networkManager.value,
            coroutineScope,
        ).adapt(
            effAdapter = { (it as? TopLevelFeature.Eff.ExpertsEff)?.eff },
            msgAdapter = { TopLevelFeature.Msg.ExpertsMsg(it) }
        )
    listOf(
        networkManager.value
            .addListener(createWebSocketMessageHandler(listener)),
        effectHandler,
        networkManager.value,
    ).forEach(sessionCloseableContainer::addCloseableChild)
    feature.wrapWithEffectHandler(effectHandler)
    platform.dataStore.authToken = token
    listener.invoke(TopLevelFeature.Msg.LoggedIn)
}

fun FeatureProvider.logOut(listener: (TopLevelFeature.Msg) -> Unit) {
    sessionCloseableContainer.close()
    platform.dataStore.authToken = null
    listener.invoke(TopLevelFeature.Msg.OpenLoginScreen)
}