package com.well.sharedMobile.puerh._featureProvider

import com.well.modules.atomic.asCloseable
import com.well.modules.db.users.insertOrReplace
import com.well.modules.db.users.usersPresenceFlow
import com.well.modules.models.AuthResponse
import com.well.modules.models.User
import com.well.modules.models.WebSocketMsg
import com.well.modules.utils.dataStore.AuthInfo
import com.well.modules.utils.dataStore.authInfo
import com.well.modules.utils.puerh.EffectHandler
import com.well.modules.utils.puerh.adapt
import com.well.modules.utils.puerh.wrapWithEffectHandler
import com.well.sharedMobile.networking.NetworkManager
import com.well.sharedMobile.puerh._topLevel.Alert
import com.well.sharedMobile.puerh._topLevel.ScreenState
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature
import com.well.sharedMobile.puerh.experts.ExpertsApiEffectHandler
import com.well.sharedMobile.puerh.login.LoginFeature
import com.well.sharedMobile.puerh.login.SocialNetwork
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
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
    val authInfo = authResponse.toAuthInfo()
    if (!authResponse.user.initialized) {
        nonInitializedAuthInfo.value = authInfo
        networkManager = NetworkManager(
            authResponse.token,
            startWebSocket = false,
            unauthorizedHandler = {
                sessionInfo = null
            })
        sessionInfo!!.addCloseableChild(networkManager)
        listener.invoke(
            TopLevelFeature.Msg.Push(
                ScreenState.MyProfile(
                    MyProfileFeature.initialState(
                        isCurrent = true,
                        authResponse.user
                    )
                )
            )
        )
    } else {
        loggedIn(authInfo, user = authResponse.user, listener = listener)
    }
}

fun FeatureProvider.loggedIn(
    authInfo: AuthInfo,
    user: User? = null,
    listener: (TopLevelFeature.Msg) -> Unit,
) {
    sessionInfo = SessionInfo(authInfo.id)
    user?.let(database.usersQueries::insertOrReplace)
    networkManager = NetworkManager(authInfo.token, startWebSocket = true, unauthorizedHandler = {
        logOut(listener)
    })
    val effectHandler: EffectHandler<TopLevelFeature.Eff, TopLevelFeature.Msg> =
        ExpertsApiEffectHandler(
            networkManager,
            database,
            coroutineScope,
        ).adapt(
            effAdapter = { (it as? TopLevelFeature.Eff.ExpertsEff)?.eff },
            msgAdapter = { TopLevelFeature.Msg.ExpertsMsg(it) }
        )
    listOf(
        networkManager
            .addListener(createWebSocketMessageHandler(listener)),
        notifyUsersDBPresenceCloseable(),
        effectHandler,
        networkManager,
    ).forEach(sessionInfo!!::addCloseableChild)
    feature.wrapWithEffectHandler(effectHandler)
    platform.dataStore.authInfo = authInfo
    listener.invoke(TopLevelFeature.Msg.LoggedIn(authInfo.id))
}

fun FeatureProvider.logOut(listener: (TopLevelFeature.Msg) -> Unit) {
    sessionInfo = null
    platform.dataStore.authInfo = null
    database.usersQueries.deleteAll()
    listener.invoke(TopLevelFeature.Msg.OpenLoginScreen)
}

private fun AuthResponse.toAuthInfo() = AuthInfo(token = token, id = user.id)

private fun FeatureProvider.notifyUsersDBPresenceCloseable() =
    coroutineScope.launch {
        networkManager.state
            .filter { it == NetworkManager.Status.Connected }
            .combine(database.usersQueries.usersPresenceFlow()) { _, usersPresence ->
                WebSocketMsg.Front.SetUsersPresence(usersPresence)
            }
            .collect(networkManager::send)
    }.asCloseable()