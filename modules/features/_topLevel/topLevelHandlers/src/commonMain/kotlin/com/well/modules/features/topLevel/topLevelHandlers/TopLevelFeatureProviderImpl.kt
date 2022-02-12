package com.well.modules.features.topLevel.topLevelHandlers

import com.well.modules.atomic.AtomicCloseableLateInitRef
import com.well.modules.atomic.AtomicCloseableRef
import com.well.modules.atomic.AtomicLateInitRef
import com.well.modules.atomic.AtomicMutableList
import com.well.modules.atomic.AtomicRef
import com.well.modules.atomic.Closeable
import com.well.modules.atomic.CloseableContainer
import com.well.modules.atomic.asCloseable
import com.well.modules.atomic.freeze
import com.well.modules.db.chatMessages.delete
import com.well.modules.db.chatMessages.getByIdFlow
import com.well.modules.db.chatMessages.insert
import com.well.modules.db.chatMessages.lastListFlow
import com.well.modules.db.chatMessages.messagePresenceFlow
import com.well.modules.db.chatMessages.selectAllFlow
import com.well.modules.db.chatMessages.unreadCountsFlow
import com.well.modules.db.meetings.insertAll
import com.well.modules.db.meetings.listFlow
import com.well.modules.db.meetings.removeAll
import com.well.modules.db.mobile.DatabaseProvider
import com.well.modules.db.mobile.createDatabaseProvider
import com.well.modules.db.users.getByIdFlow
import com.well.modules.db.users.getByIdsFlow
import com.well.modules.db.users.getFavoritesFlow
import com.well.modules.db.users.insertOrReplace
import com.well.modules.db.users.toUser
import com.well.modules.features.calendar.calendarHandlers.CalendarEffHandler
import com.well.modules.features.call.callFeature.webRtc.WebRtcManagerI
import com.well.modules.features.chatList.chatListHandlers.ChatListEffHandler
import com.well.modules.features.experts.expertsFeature.ExpertsFeature
import com.well.modules.features.experts.expertsHandlers.ExpertsApiEffectHandler
import com.well.modules.features.login.loginFeature.LoginFeature
import com.well.modules.features.login.loginFeature.SocialNetwork
import com.well.modules.features.login.loginHandlers.credentialProviders.CredentialProvider
import com.well.modules.features.more.moreFeature.MoreFeature
import com.well.modules.features.more.moreFeature.subfeatures.AboutFeature
import com.well.modules.features.more.moreFeature.subfeatures.ActivityHistoryFeature
import com.well.modules.features.more.moreFeature.subfeatures.DonateFeature
import com.well.modules.features.more.moreFeature.subfeatures.FavoritesFeature
import com.well.modules.features.more.moreFeature.subfeatures.SupportFeature
import com.well.modules.features.more.moreFeature.subfeatures.WellAcademyFeature
import com.well.modules.features.myProfile.myProfileFeature.MyProfileFeature
import com.well.modules.features.notifications.NotificationHandler
import com.well.modules.features.topLevel.topLevelFeature.FeatureEff
import com.well.modules.features.topLevel.topLevelFeature.FeatureMsg
import com.well.modules.features.topLevel.topLevelFeature.ScreenState
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature.Eff
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature.Msg
import com.well.modules.features.userChat.userChatFeature.UserChatFeature
import com.well.modules.features.welcome.WelcomeFeature
import com.well.modules.models.FavoriteSetter
import com.well.modules.models.User
import com.well.modules.models.WebSocketMsg
import com.well.modules.networking.NetworkManager
import com.well.modules.puerhBase.ExecutorEffectHandler
import com.well.modules.puerhBase.ExecutorEffectsInterpreter
import com.well.modules.puerhBase.Feature
import com.well.modules.puerhBase.SyncFeature
import com.well.modules.puerhBase.adapt
import com.well.modules.puerhBase.wrapWithEffectHandler
import com.well.modules.utils.flowUtils.combineWithUnit
import com.well.modules.utils.flowUtils.filterNotEmpty
import com.well.modules.utils.flowUtils.mapProperty
import com.well.modules.utils.kotlinUtils.map
import com.well.modules.utils.viewUtils.Alert
import com.well.modules.utils.viewUtils.ApplicationContext
import com.well.modules.utils.viewUtils.RawNotification
import com.well.modules.utils.viewUtils.SystemContext
import com.well.modules.utils.viewUtils.WebAuthenticator
import com.well.modules.utils.viewUtils.dataStore.AuthInfo
import com.well.modules.utils.viewUtils.dataStore.authInfo
import com.well.modules.utils.viewUtils.dataStore.notificationToken
import com.well.modules.utils.viewUtils.dataStore.notificationTokenNotified
import com.well.modules.utils.viewUtils.dataStore.welcomeShowed
import com.well.modules.utils.viewUtils.napier.NapierProxy
import com.well.modules.utils.viewUtils.permissionsHandler.PermissionsHandler
import com.well.modules.utils.viewUtils.permissionsHandler.requestPermissions
import com.well.modules.utils.viewUtils.platform.Platform
import com.well.modules.utils.viewUtils.platform.isDebug
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class TopLevelFeatureProviderImpl(
    private val applicationContext: ApplicationContext,
    val webRtcManagerGenerator: (List<String>, WebRtcManagerI.Listener) -> WebRtcManagerI,
    private val providerGenerator: (SocialNetwork, SystemContext, WebAuthenticator) -> CredentialProvider,
) : Feature<Msg, TopLevelFeature.State, Eff> by SyncFeature(
    TopLevelFeature.initialState(),
    TopLevelFeature::reducer,
    Dispatchers.Default,
), DatabaseProvider by createDatabaseProvider(applicationContext) {
    init {
        NapierProxy.initializeLogging()
    }

    var sessionInfo by AtomicCloseableRef<SessionInfo>()
    private var systemService by AtomicRef<SystemService?>()
    val socialNetworkService get() = systemService?.socialNetworkService
    private val platform = Platform(applicationContext)
    var notificationHandler by AtomicRef<NotificationHandler?>()
    val dataStore get() = platform.dataStore
    val permissionsHandler get() = systemService?.permissionsHandler
    val coroutineScope = CoroutineScope(coroutineContext)
    val systemHelper get() = systemService?.context?.helper
    var networkManager by AtomicCloseableLateInitRef<NetworkManager>()
    val nonInitializedAuthInfo = AtomicLateInitRef<AuthInfo>()
    val callCloseableContainer = CloseableContainer()
    var pendingNotifications = AtomicMutableList<RawNotification>()
    private var topScreenHandlerCloseable by AtomicCloseableRef<Closeable>()
    val onlineUsersStateFlow = MutableStateFlow(emptySet<User.Id>())

    private val effectInterpreter: ExecutorEffectsInterpreter<Eff, Msg> =
        interpreter@{ eff, listener ->
            when (eff) {
                is FeatureEff.ChatList,
                is FeatureEff.MyProfile,
                is FeatureEff.Calendar,
                is FeatureEff.UpdateRequest,
                -> Unit
                is Eff.ShowAlert -> {
                    val alert = eff.alert
                    if (alert is Alert.Error) {
                        Napier.e("alert", alert.throwable)
                        Napier.e(alert.throwable.stackTraceToString())
                    }
                    systemHelper?.run {
                        MainScope().launch {
                            showAlert(alert)
                        }
                    }
                }
                is FeatureEff.Experts -> when (val expertsEff = eff.eff) {
                    is ExpertsFeature.Eff.UpdateList,
                    is ExpertsFeature.Eff.SetUserFavorite,
                    is ExpertsFeature.Eff.FilterEff,
                    -> Unit
                    is ExpertsFeature.Eff.SelectedUser -> {
                        listener.invoke(
                            Msg.PushMyProfile(
                                MyProfileFeature.initialState(
                                    isCurrent = sessionInfo!!.uid == expertsEff.user.id,
                                    user = expertsEff.user,
                                )
                            )
                        )
                    }
                }
                is FeatureEff.Call -> {
                    handleCallEff(eff.eff, listener, eff.position)
                }
                Eff.Initial -> {
                    val authInfo = dataStore.authInfo
                    if (authInfo != null) {
                        loggedIn(authInfo, listener = listener)
                    } else {
                        if (Platform.isDebug || dataStore.welcomeShowed) {
                            listener.invoke(Msg.OpenLoginScreen)
                        } else {
                            listener.invoke(Msg.OpenWelcomeScreen)
                        }
                    }
                }
                Eff.InitialLoggedIn -> {
                    val uid = sessionInfo!!.uid
                    listOf(
                        createProfileEffHandler(
                            uid = uid,
                            isCurrent = true,
                            position = TopLevelFeature.State.ScreenPosition(tab = TopLevelFeature.State.Tab.MyProfile,
                                index = 0),
                            listener = listener,
                        ),
                        CalendarEffHandler(
                            services = CalendarEffHandler.Services(
                                currentUserId = uid,
                                meetingsFlow = meetingsQueries.listFlow(),
                                getUsersByIdsFlow = usersQueries::getByIdsFlow,
                                openUserProfile = {
                                    listener(
                                        Msg.PushMyProfile(
                                            MyProfileFeature.initialState(
                                                isCurrent = false,
                                                uid = it,
                                            )
                                        )
                                    )
                                },
                                startCall = {
                                    val user = usersQueries.getById(it)
                                        .executeAsOne()
                                        .toUser()
                                    coroutineScope.launch {
                                        handleCall(user, listener)
                                    }
                                },
                                updateMeetingState = { id, state ->
                                    coroutineScope.launch {
                                        networkManager.sendFront(WebSocketMsg.Front.UpdateMeetingState(id, state))
                                    }
                                }
                            ),
                            parentCoroutineScope = coroutineScope,
                        ).adapt(
                            effAdapter = { (it as? FeatureEff.Calendar)?.eff },
                            msgAdapter = {
                                FeatureMsg.Calendar(msg = it)
                            }
                        ),
                        ExpertsApiEffectHandler(
                            services = ExpertsApiEffectHandler.Services(
                                connectionStatusFlow = networkManager.connectionStatusFlow,
                                usersListFlow = networkManager
                                    .webSocketMsgSharedFlow
                                    .filterIsInstance<WebSocketMsg.Back.ListFilteredExperts>()
                                    .mapProperty(WebSocketMsg.Back.ListFilteredExperts::userIds)
                                    .flatMapLatest(usersQueries::getByIdsFlow)
                                    .combine(onlineUsersStateFlow) { users, onlineUsers ->
                                        users.map {
                                            it.copy(isOnline = onlineUsers.contains(it.id))
                                        }
                                    }
                                    .combineWithUnit(networkManager.onConnectedFlow),
                                updateUsersFilter = {
                                    networkManager.sendFront(WebSocketMsg.Front.SetExpertsFilter(it))
                                },
                                onConnectedFlow = networkManager.onConnectedFlow,
                                setFavorite = ::updateUserFavorite,
                            ),
                            coroutineScope,
                        ).adapt(
                            effAdapter = { (it as? FeatureEff.Experts)?.eff },
                            msgAdapter = {
                                FeatureMsg.Experts(msg = it)
                            }
                        ),
                        ChatListEffHandler(
                            uid,
                            ChatListEffHandler.Services(
                                openUserChat = listener.map(Msg::OpenUserChat),
                                onConnectedFlow = networkManager.onConnectedFlow,
                                lastListViewModelFlow = messagesDatabase
                                    .lastListFlow(uid)
                                    .toChatMessageContainerFlow(uid, this@TopLevelFeatureProviderImpl),
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
                                getUsersByIdsFlow = usersQueries::getByIdsFlow,
                                sendFrontWebSocketMsg = networkManager::sendFront,
                                onlineUsersFlow = onlineUsersStateFlow,
                            ),
                            coroutineScope,
                        ).adapt(
                            effAdapter = { (it as? FeatureEff.ChatList)?.eff },
                            msgAdapter = { FeatureMsg.ChatList(it) }
                        ),
                    ).map(::wrapWithEffectHandler).forEach(sessionInfo!!::addCloseableChild)
                    notificationHandler = NotificationHandler(
                        applicationContext = applicationContext,
                        currentUid = uid,
                        services = NotificationHandler.Services(
                            lastListViewModelFlow = messagesDatabase
                                .lastListFlow(uid)
                                .toChatMessageContainerFlow(uid, this@TopLevelFeatureProviderImpl),
                            unreadCountsFlow = { messages ->
                                messagesDatabase
                                    .chatMessagesQueries
                                    .unreadCountsFlow(uid, messages)
                            },
                            getUsersByIdsFlow = usersQueries::getByIdsFlow,
                            getMessageByIdFlow = { id ->
                                messagesDatabase
                                    .getByIdFlow(id)
                                    .map { listOf(it) }
                                    .toChatMessageContainerFlow(uid, this@TopLevelFeatureProviderImpl)
                                    .filterNotEmpty()
                                    .map { it.first() }
                            },
                            openChat = {
                                Napier.d("openChat $it")
                                listener(Msg.OpenUserChat(uid = it))
                            },
                        ),
                        parentCoroutineScope = coroutineScope,
                    )
                    pendingNotifications
                        .dropAll()
                        .forEach {
                            notificationHandler?.handleRawNotification(it)
                        }
                    val notificationToken = dataStore.notificationToken
                    if (notificationToken != null && !dataStore.notificationTokenNotified) {
                        networkManager.sendFront(WebSocketMsg.Front.UpdateNotificationToken(notificationToken))
                        dataStore.notificationTokenNotified = true
                    }
                    Napier.d(eff.toString())
                }
                Eff.SystemBack -> {
                    systemService?.context?.systemBack()
                }
                is FeatureEff.Login -> {
                    when (val loginEff = eff.eff) {
                        is LoginFeature.Eff.Login -> {
                            socialNetworkLogin(loginEff.socialNetwork, listener)
                        }
                    }
                }
                is FeatureEff.Welcome -> {
                    when (eff.eff) {
                        WelcomeFeature.Eff.Continue -> {
                            if (!Platform.isDebug) {
                                dataStore.welcomeShowed = true
                            }
                            listener.invoke(Msg.OpenLoginScreen)
                        }
                    }
                }
                is FeatureEff.About -> {
                    when (val aboutEff = eff.eff) {
                        AboutFeature.Eff.Back -> {
                            listener(Msg.Pop)
                        }
                        is AboutFeature.Eff.OpenLink -> {
                            systemHelper?.openUrl(aboutEff.link)
                        }
//                        is AboutFeature.Eff.Push -> {
//                            error("${eff.eff} should be handler in screen state")
//                        }
                    }
                }
                is FeatureEff.More -> {
                    when (eff.eff) {
                        is MoreFeature.Eff.Push -> {
                            error("${eff.eff} should be handler in screen state")
                        }
                        is MoreFeature.Eff.InviteColleague -> {
                            TODO()
                        }
                    }
                }
                is FeatureEff.Support -> {
                    when (eff.eff) {
                        SupportFeature.Eff.Back -> {
                            listener(Msg.Pop)
                        }
                        is SupportFeature.Eff.Send -> {
                            listener(Msg.Pop)
                        }
                    }
                }
                is FeatureEff.WellAcademy -> {
                    when (eff.eff) {
                        WellAcademyFeature.Eff.Back -> {
                            listener(Msg.Pop)
                        }
                    }
                }
                is FeatureEff.UserChat -> {
                    when (val userChatEff = eff.eff) {
                        UserChatFeature.Eff.ChooseImage,
                        is UserChatFeature.Eff.MarkMessageRead,
                        is UserChatFeature.Eff.SendImage,
                        is UserChatFeature.Eff.SendMessage,
                        -> Unit
                        UserChatFeature.Eff.Back -> {
                            listener(Msg.Pop)
                        }
                        is UserChatFeature.Eff.Call -> {
                            listener(Msg.StartCall(userChatEff.user))
                        }
                        is UserChatFeature.Eff.OpenUserProfile -> {
                            listener(
                                Msg.PushMyProfile(
                                    MyProfileFeature.initialState(
                                        isCurrent = false,
                                        user = userChatEff.user,
                                    )
                                )
                            )
                        }
                    }
                }
                is Eff.TopScreenAppeared -> {
                    topScreenHandlerCloseable = when (val screen = eff.screen) {
                        is ScreenState.MyProfile -> {
                            if (
                                screen.state.user?.initialized != false
                                && screen.position.tab != TopLevelFeature.State.Tab.MyProfile
                            ) {
                                wrapWithEffectHandler(
                                    createProfileEffHandler(
                                        uid = screen.state.uid,
                                        position = eff.position,
                                        listener = listener,
                                    )
                                )
                            } else {
                                null
                            }
                        }
                        is ScreenState.UserChat -> {
                            wrapWithEffectHandler(
                                TopUserChatEffHandler(
                                    peerUid = screen.state.peerId,
                                    currentUid = sessionInfo!!.uid,
                                    featureProviderImpl = this@TopLevelFeatureProviderImpl,
                                    parentCoroutineScope = coroutineScope,
                                ).adapt(
                                    effAdapter = { (it as? FeatureEff.UserChat)?.eff },
                                    msgAdapter = { screen.mapMsgToTopLevel(it) }
                                )
                            )
                        }
                        is ScreenState.Favorites -> {
                            launch {
                                usersQueries
                                    .getFavoritesFlow()
                                    .map(FavoritesFeature.Msg::UpdateUsers)
                                    .map(screen::mapMsgToTopLevel)
                                    .collect(listener)
                            }.asCloseable()
                        }
                        else -> {
                            Napier.d("topScreenHandlerCloseable = null $eff")
                            null
                        }
                    }
                }
                is Eff.UpdateNotificationToken -> {
                    if (dataStore.notificationToken == eff.token) return@interpreter
                    dataStore.notificationToken = eff.token
                    if (sessionInfo != null) {
                        networkManager.sendFront(WebSocketMsg.Front.UpdateNotificationToken(eff.token))
                    } else {
                        dataStore.notificationTokenNotified = false
                    }
                }
                is Eff.UpdateSystemContext -> {
                    systemService = eff.systemContext?.let { context ->
                        SystemService(context, providerGenerator)
                    }
                    Napier.i("UpdateSystemContext $systemService")
                    permissionsHandler?.run {
                        MainScope().launch {
                            requestPermissions(*PermissionsHandler.Type.values())
                        }
                    }
                }
                is Eff.HandleRawNotification -> {
                    if (notificationHandler != null) {
                        notificationHandler?.handleRawNotification(eff.rawNotification)
                    } else if (dataStore.authInfo != null) {
                        pendingNotifications.add(eff.rawNotification)
                    }
                }
                is FeatureEff.ActivityHistory -> {
                    when (eff.eff) {
                        ActivityHistoryFeature.Eff.Back -> {
                            listener(Msg.Pop)
                        }
                    }
                }
                is FeatureEff.Donate -> {
                    when (eff.eff) {
                        DonateFeature.Eff.Back -> {
                            listener(Msg.Pop)
                        }
                        is DonateFeature.Eff.Donate -> {
                            TODO()
                        }
                    }
                }
                is FeatureEff.Favorites -> {
                    when (val favoritesEff = eff.eff) {
                        FavoritesFeature.Eff.Back -> {
                            listener(Msg.Pop)
                        }
                        is FavoritesFeature.Eff.SelectedUser -> {
                            listener.invoke(
                                Msg.PushMyProfile(
                                    MyProfileFeature.initialState(
                                        isCurrent = false,
                                        uid = favoritesEff.uid,
                                    )
                                )
                            )
                        }
                        is FavoritesFeature.Eff.UnFavoriteUser -> {
                            updateUserFavorite(
                                FavoriteSetter(
                                    favorite = false,
                                    uid = favoritesEff.uid
                                )
                            )
                        }
                    }
                }
            }
        }

    init {
        // preventing iOS InvalidMutabilityException
        val handler = ExecutorEffectHandler(
            effectInterpreter,
            coroutineScope,
        )
        wrapWithEffectHandler(handler)
        handler.freeze()

        accept(Msg.Initial)
    }

    fun webSocketMessageHandler(
        msg: WebSocketMsg,
        listener: (Msg) -> Unit,
    ) {
        when (msg) {
            is WebSocketMsg.Front -> Unit
            is WebSocketMsg.Call.EndCall -> {
                endCall(listener)
            }
            is WebSocketMsg.Call -> Unit
            is WebSocketMsg.Back.IncomingCall -> {
                listener.invoke(Msg.IncomingCall(msg))
                coroutineScope.launch {
                    handleCallPermissions()?.also {
                        listener(Msg.EndCall)
                        listener(Msg.ShowAlert(it.first.alert))
                    }
                }
            }
            is WebSocketMsg.Back.ListFilteredExperts -> Unit
            is WebSocketMsg.Back.UpdateUsers -> {
                usersQueries.run {
                    transaction {
                        msg.users.forEach(::insertOrReplace)
                    }
                }
            }
            is WebSocketMsg.Back.UpdateCharReadPresence -> {
                messagesDatabase.lastReadMessagesQueries.run {
                    transaction {
                        msg.lastReadMessages.forEach(::insert)
                    }
                }
            }
            is WebSocketMsg.Back.UpdateMessages -> {
                messagesDatabase.run {
                    transaction {
                        msg.messages.forEach { messageInfo ->
                            messageInfo.tmpId?.let { tmpId ->
                                delete(tmpId)
                            }
                            insert(messageInfo.message)
                        }
                    }
                }
            }
            is WebSocketMsg.Back.AddMeetings -> {
                meetingsQueries
                    .insertAll(msg.meetings)
            }
            is WebSocketMsg.Back.RemovedMeetings -> {
                meetingsQueries
                    .removeAll(msg.ids)
            }
            is WebSocketMsg.Back.RemovedUsers -> {
                msg.ids.forEach { id ->
                    usersQueries.transaction {
                        meetingsQueries.transaction {
                            messagesDatabase.transaction {
                                usersQueries.deleteById(id)
                                meetingsQueries.clearUser(id)
                                messagesDatabase.chatMessagesQueries.clearUser(id)
                            }
                        }
                    }
                }
            }
            WebSocketMsg.Back.NotificationTokenRequest -> {
                dataStore.notificationToken?.let { notificationToken ->
                    coroutineScope.launch {
                        networkManager.sendFront(WebSocketMsg.Front.UpdateNotificationToken(notificationToken))
                    }
                }
            }
            is WebSocketMsg.Back.OnlineUsersList -> {
                onlineUsersStateFlow.value = msg.ids
            }
        }
    }

    fun getFullUserByIdFlow(id: User.Id) =
        usersQueries
            .getByIdFlow(id)
            .combine(onlineUsersStateFlow) { user, onlineUsers ->
                user.copy(isOnline = onlineUsers.contains(id))
            }

    suspend fun updateUserFavorite(setter: FavoriteSetter) {
        val uid = setter.uid
        val original = usersQueries.getById(setter.uid).executeAsOne()
        usersQueries.updateFavorite(favorite = setter.favorite, id = uid)
        try {
            networkManager.setFavorite(setter)
        } catch (t: Throwable) {
            usersQueries.transaction {
                val current = usersQueries.getById(uid).executeAsOne()
                if (original.lastEdited == current.lastEdited) {
                    usersQueries.updateFavorite(favorite = original.favorite, id = original.id)
                }
            }
        }
    }
}