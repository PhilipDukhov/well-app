package com.well.sharedMobile.featureProvider

import com.well.modules.atomic.asCloseable
import com.well.modules.db.users.getByIdsFlow
import com.well.modules.db.users.insertOrReplace
import com.well.modules.db.users.usersPresenceFlow
import com.well.modules.models.AuthResponse
import com.well.modules.models.User
import com.well.modules.models.WebSocketMsg
import com.well.modules.utils.dataStore.AuthInfo
import com.well.modules.utils.dataStore.authInfo
import com.well.modules.utils.puerh.adapt
import com.well.modules.utils.puerh.wrapWithEffectHandler
import com.well.modules.networking.NetworkManager
import com.well.modules.networking.combineToNetworkConnectedState
import com.well.modules.viewHelpers.Alert
import com.well.sharedMobile.TopLevelFeature
import com.well.modules.features.chatList.chatListHandlers.ChatListEffHandler
import com.well.modules.features.experts.expertsHandlers.ExpertsApiEffectHandler
import com.well.modules.features.login.LoginFeature
import com.well.modules.features.login.SocialNetwork
import com.well.modules.features.myProfile.MyProfileFeature
import com.well.modules.utils.puerh.EffectHandler
import com.well.sharedMobile.ScreenState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal suspend fun FeatureProviderImpl.socialNetworkLogin(
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

internal fun FeatureProviderImpl.gotAuthResponse(
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

internal fun FeatureProviderImpl.loggedIn(
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
            ChatListEffHandler.Services(
                openUserChat = {
                    listener(TopLevelFeature.Msg.OpenUserChat(it))
                },
                onConnectedFlow = networkManager.onConnectedFlow,
                sendFrontWebSocketMsg = networkManager::sendFront,
                getUsersByIds = usersDatabase.usersQueries::getByIdsFlow
            ),
            messagesDatabase,
            scope,
        ).adapt(
            effAdapter = { (it as? TopLevelFeature.Eff.ChatListEff)?.eff },
            msgAdapter = { TopLevelFeature.Msg.ChatListMsg(it) }
        ),
    )
    val webSocketListenerCloseable = coroutineScope.launch {
        networkManager
            .webSocketMsgSharedFlow
            .collect {
                webSocketMessageHandler(it, listener)
            }
    }.asCloseable()
    (effectHandlers.map(::wrapWithEffectHandler) + listOf(
        webSocketListenerCloseable,
        notifyUsersDBPresenceCloseable(),
        networkManager,
        scope.asCloseable(),
    )).forEach(sessionInfo!!::addCloseableChild)
    platform.dataStore.authInfo = authInfo
    listener.invoke(TopLevelFeature.Msg.LoggedIn(authInfo.id))
}

internal fun FeatureProviderImpl.logOut(listener: (TopLevelFeature.Msg) -> Unit) {
    sessionInfo = null
    platform.dataStore.authInfo = null
    databaseManager.clear()
    listener.invoke(TopLevelFeature.Msg.OpenLoginScreen)
}

private fun AuthResponse.toAuthInfo() = AuthInfo(token = token, id = user.id)

private fun FeatureProviderImpl.notifyUsersDBPresenceCloseable() =
    coroutineScope.launch {
        usersDatabase.usersQueries.usersPresenceFlow()
            .combineToNetworkConnectedState(networkManager)
            .map { usersPresence ->
                WebSocketMsg.Front.SetUsersPresence(usersPresence)
            }
            .collect(networkManager::sendFront)
    }.asCloseable()