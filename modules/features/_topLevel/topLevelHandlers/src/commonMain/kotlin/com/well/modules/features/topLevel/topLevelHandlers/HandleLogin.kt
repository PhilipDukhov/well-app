package com.well.modules.features.topLevel.topLevelHandlers

import com.well.modules.atomic.asCloseable
import com.well.modules.db.meetings.listIdsFlow
import com.well.modules.db.users.insertOrReplace
import com.well.modules.db.users.usersPresenceFlow
import com.well.modules.features.login.loginFeature.LoginFeature
import com.well.modules.features.login.loginFeature.SocialNetwork
import com.well.modules.features.myProfile.myProfileFeature.MyProfileFeature
import com.well.modules.features.topLevel.topLevelFeature.FeatureMsg
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature.State.ScreenPosition
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature.State.Tab
import com.well.modules.models.AuthResponse
import com.well.modules.models.User
import com.well.modules.models.WebSocketMsg
import com.well.modules.networking.NetworkManager
import com.well.modules.puerhBase.Listener
import com.well.modules.utils.flowUtils.collectIn
import com.well.modules.utils.flowUtils.combineWithUnit
import com.well.modules.utils.viewUtils.Alert
import com.well.modules.utils.viewUtils.dataStore.AuthInfo
import com.well.modules.utils.viewUtils.dataStore.authInfo
import com.well.modules.utils.viewUtils.dataStore.deviceUid
import com.well.modules.utils.viewUtils.dataStore.notificationToken
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal suspend fun TopLevelFeatureProviderImpl.socialNetworkLogin(
    socialNetwork: SocialNetwork,
    listener: Listener<TopLevelFeature.Msg>,
) {
    try {
        socialNetworkService?.login(socialNetwork)?.let { authResponse ->
            gotAuthResponse(authResponse, listener)
        }
    } catch (t: Throwable) {
        if (t !is CancellationException && t.message?.contains("com.well.modules.utils error 0") != true) {
            listener.invoke(TopLevelFeature.Msg.ShowAlert(Alert.Error.fixDescription(t)))
        }
    } finally {
        listener.invoke(
            FeatureMsg.Login(
                msg = LoginFeature.Msg.LoginAttemptFinished,
                position = ScreenPosition(
                    tab = Tab.Login,
                    index = 0
                )
            )
        )
    }
}

internal fun TopLevelFeatureProviderImpl.gotAuthResponse(
    authResponse: AuthResponse,
    listener: Listener<TopLevelFeature.Msg>,
) {
    val authInfo = authResponse.toAuthInfo()
    if (!authResponse.user.initialized) {
        nonInitializedAuthInfo.value = authInfo
        networkManager = NetworkManager(
            token = authResponse.token,
            deviceId = dataStore.deviceUid,
            startWebSocket = false,
            unauthorizedHandler = {
                sessionInfo = null
            },
        )
        listener.invoke(
            TopLevelFeature.Msg.PushMyProfile(
                MyProfileFeature.initialState(
                    isCurrent = true,
                    authResponse.user
                )
            )
        )
    } else {
        loggedIn(authInfo, user = authResponse.user, listener = listener)
    }
}

internal fun TopLevelFeatureProviderImpl.loggedIn(
    authInfo: AuthInfo,
    user: User? = null,
    listener: Listener<TopLevelFeature.Msg>,
) {
    val uid = authInfo.id
    sessionInfo = SessionInfo(uid)
    openDatabase()
    user?.let(usersQueries::insertOrReplace)
    networkManager = NetworkManager(
        token = authInfo.token,
        deviceId = dataStore.deviceUid,
        startWebSocket = true,
        unauthorizedHandler = {
            logOut(listener)
        },
    )
    listener.invoke(TopLevelFeature.Msg.LoggedIn(uid))
    val webSocketListenerCloseable = networkManager
        .webSocketMsgSharedFlow
        .collectIn(coroutineScope) {
            webSocketMessageHandler(it, listener)
        }
        .asCloseable()
    val meetingsPresenceCloseable = meetingsQueries
        .listIdsFlow()
        .combineWithUnit(networkManager.onConnectedFlow)
        .map(WebSocketMsg.Front::SetMeetingsPresence)
        .collectIn(coroutineScope, networkManager::sendFront)
        .asCloseable()
    listOf(
        webSocketListenerCloseable,
        notifyUsersDBPresenceCloseable(),
        networkManager,
        meetingsPresenceCloseable,
    ).forEach(sessionInfo!!::addCloseableChild)
    dataStore.authInfo = authInfo
}

internal fun TopLevelFeatureProviderImpl.logOut(listener: (TopLevelFeature.Msg) -> Unit) {
    coroutineScope.launch {
        notificationHandler?.clearAllNotifications()
        notificationHandler = null
        val token = dataStore.notificationToken
        if (token != null) {
            networkManager.sendFront(WebSocketMsg.Front.Logout)
        }
//        networkManager.webSocketMsgSharedFlow
//            .filterIsInstance<WebSocketMsg.Back.LogoutConfirmation>()
//            .first()
        sessionInfo = null
        dataStore.authInfo = null
        pendingNotifications.dropAll()
        clearDatabase()
        listener.invoke(TopLevelFeature.Msg.OpenLoginScreen)
    }
}

private fun AuthResponse.toAuthInfo() = AuthInfo(token = token, id = user.id)

private fun TopLevelFeatureProviderImpl.notifyUsersDBPresenceCloseable() =
    coroutineScope.launch {
        usersQueries.usersPresenceFlow()
            .combineWithUnit(networkManager.onConnectedFlow)
            .map(WebSocketMsg.Front::SetUsersPresence)
            .collect(networkManager::sendFront)
    }.asCloseable()
