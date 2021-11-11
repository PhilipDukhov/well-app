package com.well.sharedMobile.featureProvider

import com.well.modules.atomic.asCloseable
import com.well.modules.db.chatMessages.lastListWithStatusFlow
import com.well.modules.db.chatMessages.messagePresenceFlow
import com.well.modules.db.chatMessages.selectAllFlow
import com.well.modules.db.chatMessages.unreadCountsFlow
import com.well.modules.db.users.getByIdsFlow
import com.well.modules.db.users.insertOrReplace
import com.well.modules.db.users.usersPresenceFlow
import com.well.modules.features.chatList.chatListFeature.ChatListFeature
import com.well.modules.features.chatList.chatListHandlers.ChatListEffHandler
import com.well.modules.features.experts.expertsFeature.ExpertsFeature
import com.well.modules.features.experts.expertsHandlers.ExpertsApiEffectHandler
import com.well.modules.features.login.loginFeature.LoginFeature
import com.well.modules.features.login.loginFeature.SocialNetwork
import com.well.modules.features.more.MoreFeature
import com.well.modules.features.myProfile.myProfileFeature.MyProfileFeature
import com.well.modules.models.AuthResponse
import com.well.modules.models.User
import com.well.modules.models.UserId
import com.well.modules.models.WebSocketMsg
import com.well.modules.networking.NetworkManager
import com.well.modules.puerhBase.EffectHandler
import com.well.modules.puerhBase.Listener
import com.well.modules.puerhBase.adapt
import com.well.modules.puerhBase.wrapWithEffectHandler
import com.well.modules.utils.flowUtils.combineWithUnit
import com.well.modules.utils.flowUtils.mapProperty
import com.well.modules.utils.kotlinUtils.map
import com.well.modules.utils.viewUtils.Alert
import com.well.modules.utils.viewUtils.dataStore.AuthInfo
import com.well.modules.utils.viewUtils.dataStore.authInfo
import com.well.sharedMobile.FeatureEff
import com.well.sharedMobile.FeatureMsg
import com.well.sharedMobile.ScreenState
import com.well.sharedMobile.TopLevelFeature
import com.well.sharedMobile.TopLevelFeature.State.ScreenPosition
import com.well.sharedMobile.TopLevelFeature.State.Tab
import com.well.sharedMobile.createTab
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
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
    databaseManager.open()
    user?.let(usersDatabase.usersQueries::insertOrReplace)
    networkManager = NetworkManager(authInfo.token, startWebSocket = true, unauthorizedHandler = {
        logOut(listener)
    })
    listener.invoke(TopLevelFeature.Msg.LoggedIn(uid))
    val scope = CoroutineScope(coroutineContext)
    val effectHandlers: List<EffectHandler<TopLevelFeature.Eff, TopLevelFeature.Msg>> = listOf(
        createProfileEffHandler(
            uid = uid,
            position = ScreenPosition(tab = Tab.MyProfile, index = 0),
            listener = listener,
        ),
        ExpertsApiEffectHandler(
            services = ExpertsApiEffectHandler.Services(
                connectionStatusFlow = networkManager.connectionStatusFlow,
                usersListFlow = networkManager
                    .webSocketMsgSharedFlow
                    .filterIsInstance<WebSocketMsg.Back.ListFilteredExperts>()
                    .mapProperty(WebSocketMsg.Back.ListFilteredExperts::userIds)
                    .flatMapLatest(usersDatabase.usersQueries::getByIdsFlow)
                    .combineWithUnit(networkManager.onConnectedFlow),
                updateUsersFilter = {
                    networkManager.sendFront(WebSocketMsg.Front.SetExpertsFilter(it))
                },
                onConnectedFlow = networkManager.onConnectedFlow,
                setFavorite = networkManager::setFavorite,
            ),
            coroutineScopeArg = scope,
        ).adapt(
            effAdapter = { (it as? FeatureEff.Experts)?.eff },
            msgAdapter = {
                FeatureMsg.Experts(msg = it)
            }
        ),
        ChatListEffHandler(
            uid,
            ChatListEffHandler.Services(
                openUserChat = listener.map(TopLevelFeature.Msg::OpenUserChat),
                onConnectedFlow = networkManager.onConnectedFlow,
                lastListWithStatusFlow = messagesDatabase.lastListWithStatusFlow(uid),
                unreadCountsFlow = { messages ->
                    messagesDatabase
                        .chatMessagesQueries
                        .unreadCountsFlow(uid, messages)
                },
                lastPresentMessageIdFlow = messagesDatabase
                    .chatMessagesQueries
                    .messagePresenceFlow(),
                lastReadPresenceFlow = messagesDatabase
                    .lastReadMessagesQueries
                    .selectAllFlow(),
                getUsersByIdsFlow = usersDatabase.usersQueries::getByIdsFlow,
                sendFrontWebSocketMsg = networkManager::sendFront,
            ),
            scope,
        ).adapt(
            effAdapter = { (it as? FeatureEff.ChatList)?.eff },
            msgAdapter = { FeatureMsg.ChatList(it) }
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
}

internal fun FeatureProviderImpl.logOut(listener: (TopLevelFeature.Msg) -> Unit) {
    sessionInfo = null
    platform.dataStore.authInfo = null
    databaseManager.clear()
    listener.invoke(TopLevelFeature.Msg.OpenLoginScreen)
}

internal fun loggedInTabs(uid: UserId) = mapOf(
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
        usersDatabase.usersQueries.usersPresenceFlow()
            .combineWithUnit(networkManager.onConnectedFlow)
            .map(WebSocketMsg.Front::SetUsersPresence)
            .collect(networkManager::sendFront)
    }.asCloseable()
