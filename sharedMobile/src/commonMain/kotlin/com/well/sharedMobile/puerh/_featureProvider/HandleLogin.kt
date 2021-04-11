package com.well.sharedMobile.puerh._featureProvider

import com.well.modules.models.User
import com.well.sharedMobile.networking.NetworkManager
import com.well.sharedMobile.puerh._topLevel.Alert
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature
import com.well.sharedMobile.puerh.login.LoginFeature
import com.well.sharedMobile.puerh.login.SocialNetwork
import com.well.sharedMobile.puerh.onlineUsers.OnlineUsersApiEffectHandler
import com.well.modules.utils.dataStore.authToken
import com.well.modules.utils.dataStore.deviceUUID
import com.well.modules.utils.puerh.EffectHandler
import com.well.modules.utils.puerh.adapt
import com.well.modules.utils.puerh.wrapWithEffectHandler
import com.well.modules.utils.randomUUIDString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

fun FeatureProvider.socialNetworkLogin(
    socialNetwork: SocialNetwork,
    listener: (TopLevelFeature.Msg) -> Unit
) {
    MainScope().launch {
        try {
            val (token, user) = socialNetworkService.login(socialNetwork)
            coroutineScope.launch {
                gotUser(user, token, listener)
            }
        } catch (t: Throwable) {
            if (t !is CancellationException && t.message?.contains("com.well.modules.utils error 0") != true) {
                coroutineScope.launch {
                    listener.invoke(TopLevelFeature.Msg.ShowAlert(Alert.Throwable(t)))
                }
            }
        } finally {
            coroutineScope.launch {
                listener.invoke(TopLevelFeature.Msg.LoginMsg(LoginFeature.Msg.LoginAttemptFinished))
            }
        }
    }
}

suspend fun FeatureProvider.getTestLoginTokenAndUser(): Pair<String, User> {
    val deviceUUID = platform.dataStore.deviceUUID
        ?: run {
            randomUUIDString().also {
                platform.dataStore.deviceUUID = it
            }
        }
    return socialNetworkService
        .testLogin(deviceUUID)
}

fun FeatureProvider.gotUser(
    user: User,
    token: String,
    listener: (TopLevelFeature.Msg) -> Unit,
) {
    if (!user.initialized) {
        nonInitializedUserToken.value = token
        sessionCloseableContainer.close()
        networkManager.value = NetworkManager(token, startWebSocket = false, unauthorizedHandler = {
            sessionCloseableContainer.close()
        })
        sessionCloseableContainer.addCloseableChild(networkManager.value)
        listener.invoke(TopLevelFeature.Msg.OpenUserProfile(user, isCurrent = true))
    } else {
        loggedIn(token, listener)
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
        OnlineUsersApiEffectHandler(
            networkManager.value,
            coroutineScope,
        ).adapt(
            effAdapter = { (it as? TopLevelFeature.Eff.OnlineUsersEff)?.eff },
            msgAdapter = { TopLevelFeature.Msg.OnlineUsersMsg(it) }
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