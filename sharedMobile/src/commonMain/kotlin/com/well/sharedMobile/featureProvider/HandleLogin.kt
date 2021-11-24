package com.well.sharedMobile.featureProvider

import com.well.modules.atomic.asCloseable
import com.well.modules.db.meetings.listIdsFlow
import com.well.modules.db.users.insertOrReplace
import com.well.modules.db.users.usersPresenceFlow
import com.well.modules.features.chatList.chatListFeature.ChatListFeature
import com.well.modules.features.experts.expertsFeature.ExpertsFeature
import com.well.modules.features.login.loginFeature.LoginFeature
import com.well.modules.features.login.loginFeature.SocialNetwork
import com.well.modules.features.more.MoreFeature
import com.well.modules.features.myProfile.myProfileFeature.MyProfileFeature
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
import com.well.sharedMobile.FeatureMsg
import com.well.sharedMobile.ScreenState
import com.well.sharedMobile.TopLevelFeature
import com.well.sharedMobile.TopLevelFeature.State.ScreenPosition
import com.well.sharedMobile.TopLevelFeature.State.Tab
import com.well.sharedMobile.createTab
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal suspend fun FeatureProviderImpl.socialNetworkLogin(
    socialNetwork: SocialNetwork,
    listener: Listener<TopLevelFeature.Msg>,
) {
    try {
        gotAuthResponse(socialNetworkService.login(socialNetwork), listener)
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

internal fun FeatureProviderImpl.gotAuthResponse(
    authResponse: AuthResponse,
    listener: Listener<TopLevelFeature.Msg>,
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

internal fun FeatureProviderImpl.loggedIn(
    authInfo: AuthInfo,
    user: User? = null,
    listener: Listener<TopLevelFeature.Msg>,
) {
    val uid = authInfo.id
    sessionInfo = SessionInfo(uid)
    openDatabase()
    user?.let(usersQueries::insertOrReplace)
    networkManager = NetworkManager(authInfo.token, startWebSocket = true, unauthorizedHandler = {
        logOut(listener)
    })
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
    platform.dataStore.authInfo = authInfo
}

internal fun FeatureProviderImpl.logOut(listener: (TopLevelFeature.Msg) -> Unit) {
    sessionInfo = null
    platform.dataStore.authInfo = null
    clearDatabase()
    listener.invoke(TopLevelFeature.Msg.OpenLoginScreen)
}

internal fun loggedInTabs(uid: User.Id) = mapOf(
    createTab(
        Tab.MyProfile,
        state = MyProfileFeature.initialState(
            isCurrent = true,
            uid = uid
        ),
        createScreen = ScreenState::MyProfile,
    ),
    createTab(
        tab = Tab.Experts,
        state = ExpertsFeature.initialState(),
        createScreen = ScreenState::Experts,
    ),
    createTab(
        tab = Tab.ChatList,
        state = ChatListFeature.State(),
        createScreen = ScreenState::ChatList,
    ),
    createTab(
        tab = Tab.More,
        state = MoreFeature.State(),
        createScreen = ScreenState::More,
    ),
)

private fun AuthResponse.toAuthInfo() = AuthInfo(token = token, id = user.id)

private fun FeatureProviderImpl.notifyUsersDBPresenceCloseable() =
    coroutineScope.launch {
        usersQueries.usersPresenceFlow()
            .combineWithUnit(networkManager.onConnectedFlow)
            .map(WebSocketMsg.Front::SetUsersPresence)
            .collect(networkManager::sendFront)
    }.asCloseable()
