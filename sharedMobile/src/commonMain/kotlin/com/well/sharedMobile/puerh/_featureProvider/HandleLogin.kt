package com.well.sharedMobile.puerh._featureProvider

import com.well.modules.atomic.asCloseable
import com.well.modules.db.chatMessages.insert
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
import com.well.sharedMobile.networking.combineToNetworkConnectedState
import com.well.sharedMobile.puerh._topLevel.Alert
import com.well.sharedMobile.puerh._topLevel.ScreenState
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature
import com.well.sharedMobile.puerh.chatList.ChatListEffHandler
import com.well.sharedMobile.puerh.experts.ExpertsApiEffectHandler
import com.well.sharedMobile.puerh.login.LoginFeature
import com.well.sharedMobile.puerh.login.SocialNetwork
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

suspend fun FeatureProvider.socialNetworkLogin(
    socialNetwork: SocialNetwork,
    listener: (TopLevelFeature.Msg) -> Unit,
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
    databaseManager.open()
    user?.let(usersDatabase.usersQueries::insertOrReplace)
    networkManager = NetworkManager(authInfo.token, startWebSocket = true, unauthorizedHandler = {
        logOut(listener)
    })
    val scope = CoroutineScope(coroutineContext)
    val effectHandlers = listOf<EffectHandler<TopLevelFeature.Eff, TopLevelFeature.Msg>>(
        ExpertsApiEffectHandler(
            networkManager,
            usersDatabase,
            scope,
        ).adapt(
            effAdapter = { (it as? TopLevelFeature.Eff.ExpertsEff)?.eff },
            msgAdapter = { TopLevelFeature.Msg.ExpertsMsg(it) }
        ),
        ChatListEffHandler(
            authInfo.id,
            networkManager,
            usersDatabase,
            messagesDatabase,
            scope,
        ),
    )
    (effectHandlers.map(feature::wrapWithEffectHandler) + listOf(
        networkManager
            .addListener(createWebSocketMessageHandler(listener)),
        notifyUsersDBPresenceCloseable(),
        networkManager,
        scope.asCloseable(),
    )).forEach(sessionInfo!!::addCloseableChild)
    platform.dataStore.authInfo = authInfo
    listener.invoke(TopLevelFeature.Msg.LoggedIn(authInfo.id))
}

fun FeatureProvider.logOut(listener: (TopLevelFeature.Msg) -> Unit) {
    sessionInfo = null
    platform.dataStore.authInfo = null
    databaseManager.clear()
    listener.invoke(TopLevelFeature.Msg.OpenLoginScreen)
}

private fun AuthResponse.toAuthInfo() = AuthInfo(token = token, id = user.id)

private fun FeatureProvider.notifyUsersDBPresenceCloseable() =
    coroutineScope.launch {
        usersDatabase.usersQueries.usersPresenceFlow()
            .combineToNetworkConnectedState(networkManager)
            .map { usersPresence ->
                WebSocketMsg.Front.SetUsersPresence(usersPresence)
            }
            .collect(networkManager::send)
    }.asCloseable()