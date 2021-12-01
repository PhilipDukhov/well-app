package com.well.modules.features.topLevel.topLevelHandlers

import com.well.modules.atomic.AtomicCloseableLateInitRef
import com.well.modules.atomic.AtomicCloseableRef
import com.well.modules.atomic.AtomicLateInitRef
import com.well.modules.atomic.Closeable
import com.well.modules.atomic.CloseableContainer
import com.well.modules.atomic.freeze
import com.well.modules.db.chatMessages.delete
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
import com.well.modules.db.users.getByIdsFlow
import com.well.modules.db.users.insertOrReplace
import com.well.modules.db.users.toUser
import com.well.modules.features.calendar.calendarHandlers.CalendarEffHandler
import com.well.modules.features.call.callFeature.webRtc.WebRtcManagerI
import com.well.modules.features.chatList.chatListHandlers.ChatListEffHandler
import com.well.modules.features.experts.expertsFeature.ExpertsFeature
import com.well.modules.features.experts.expertsHandlers.ExpertsApiEffectHandler
import com.well.modules.features.login.loginFeature.LoginFeature
import com.well.modules.features.login.loginFeature.SocialNetwork
import com.well.modules.features.login.loginHandlers.SocialNetworkService
import com.well.modules.features.login.loginHandlers.credentialProviders.CredentialProvider
import com.well.modules.features.login.loginHandlers.credentialProviders.OAuthCredentialProvider
import com.well.modules.features.more.MoreFeature
import com.well.modules.features.more.about.AboutFeature
import com.well.modules.features.more.support.SupportFeature
import com.well.modules.features.myProfile.myProfileFeature.MyProfileFeature
import com.well.modules.features.topLevel.topLevelFeature.FeatureEff
import com.well.modules.features.topLevel.topLevelFeature.FeatureMsg
import com.well.modules.features.topLevel.topLevelFeature.ScreenState
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature.Eff
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature.Msg
import com.well.modules.features.userChat.userChatFeature.UserChatFeature
import com.well.modules.features.welcome.WelcomeFeature
import com.well.modules.models.WebSocketMsg
import com.well.modules.networking.NetworkManager
import com.well.modules.puerhBase.ExecutorEffectHandler
import com.well.modules.puerhBase.ExecutorEffectsInterpreter
import com.well.modules.puerhBase.Feature
import com.well.modules.puerhBase.SyncFeature
import com.well.modules.puerhBase.adapt
import com.well.modules.puerhBase.wrapWithEffectHandler
import com.well.modules.utils.flowUtils.combineWithUnit
import com.well.modules.utils.flowUtils.mapProperty
import com.well.modules.utils.kotlinUtils.map
import com.well.modules.utils.viewUtils.Alert
import com.well.modules.utils.viewUtils.AppContext
import com.well.modules.utils.viewUtils.ContextHelper
import com.well.modules.utils.viewUtils.WebAuthenticator
import com.well.modules.utils.viewUtils.dataStore.AuthInfo
import com.well.modules.utils.viewUtils.dataStore.authInfo
import com.well.modules.utils.viewUtils.dataStore.welcomeShowed
import com.well.modules.utils.viewUtils.permissionsHandler.PermissionsHandler
import com.well.modules.utils.viewUtils.platform.Platform
import com.well.modules.utils.viewUtils.platform.isDebug
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

internal class TopLevelFeatureProviderImpl(
    private val appContext: AppContext,
    val webRtcManagerGenerator: (List<String>, WebRtcManagerI.Listener) -> WebRtcManagerI,
    providerGenerator: (SocialNetwork, AppContext, WebAuthenticator) -> CredentialProvider,
) : Feature<Msg, TopLevelFeature.State, Eff> by SyncFeature(
    TopLevelFeature.initialState(),
    TopLevelFeature::reducer,
    Dispatchers.Default
), DatabaseProvider by createDatabaseProvider(appContext) {
    var sessionInfo by AtomicCloseableRef<SessionInfo>()
    val socialNetworkService = SocialNetworkService { network ->
        when (network) {
            SocialNetwork.Twitter -> {
                OAuthCredentialProvider("twitter", contextHelper)
            }
            else -> providerGenerator(network, appContext, contextHelper)
        }
    }
    val platform = Platform(appContext)
    val permissionsHandler = PermissionsHandler(appContext.permissionsHandlerContext)
    val coroutineScope = CoroutineScope(coroutineContext)
    val contextHelper = ContextHelper(appContext)
    var networkManager by AtomicCloseableLateInitRef<NetworkManager>()
    val nonInitializedAuthInfo = AtomicLateInitRef<AuthInfo>()
    val callCloseableContainer = CloseableContainer()
    private var topScreenHandlerCloseable by AtomicCloseableRef<Closeable>()

    private val effectInterpreter: ExecutorEffectsInterpreter<Eff, Msg> =
        interpreter@{ eff, listener ->
            when (eff) {
                is FeatureEff.ChatList,
                is FeatureEff.MyProfile,
                -> Unit
                is Eff.ShowAlert -> {
                    val alert = eff.alert
                    if (alert is Alert.Error) {
                        Napier.e("alert", alert.throwable)
                        Napier.e(alert.throwable.stackTraceToString())
                    }
                    MainScope().launch {
                        contextHelper.showAlert(alert)
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
                    is ExpertsFeature.Eff.CallUser -> {
                        handleCall(expertsEff.user, listener)
                    }
                }
                is FeatureEff.Call -> {
                    handleCallEff(eff.eff, listener, eff.position)
                }
                Eff.Initial -> {
                    val authInfo = platform.dataStore.authInfo
                    if (authInfo != null) {
                        loggedIn(authInfo, listener = listener)
                    } else {
                        if (Platform.isDebug || platform.dataStore.welcomeShowed) {
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
                        ExpertsApiEffectHandler(
                            services = ExpertsApiEffectHandler.Services(
                                connectionStatusFlow = networkManager.connectionStatusFlow,
                                usersListFlow = networkManager
                                    .webSocketMsgSharedFlow
                                    .filterIsInstance<WebSocketMsg.Back.ListFilteredExperts>()
                                    .mapProperty(WebSocketMsg.Back.ListFilteredExperts::userIds)
                                    .flatMapLatest(usersQueries::getByIdsFlow)
                                    .combineWithUnit(networkManager.onConnectedFlow),
                                updateUsersFilter = {
                                    networkManager.sendFront(WebSocketMsg.Front.SetExpertsFilter(it))
                                },
                                onConnectedFlow = networkManager.onConnectedFlow,
                                setFavorite = networkManager::setFavorite,
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
                            ),
                            coroutineScope,
                        ).adapt(
                            effAdapter = { (it as? FeatureEff.ChatList)?.eff },
                            msgAdapter = { FeatureMsg.ChatList(it) }
                        ),
                    ).map(::wrapWithEffectHandler).forEach(sessionInfo!!::addCloseableChild)
                }
                Eff.SystemBack -> {
                    appContext.systemBack()
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
//                            platform.dataStore.welcomeShowed = true
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
                            contextHelper.openUrl(aboutEff.link)
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
                    when (val screen = eff.screen) {
                        is ScreenState.MyProfile -> {
                            if (
                                screen.state.user?.initialized != false
                                && screen.position.tab != TopLevelFeature.State.Tab.MyProfile
                            ) {
                                topScreenHandlerCloseable = wrapWithEffectHandler(
                                    createProfileEffHandler(
                                        uid = screen.state.uid,
                                        position = eff.position,
                                        listener = listener,
                                    )
                                )
                            }
                        }
                        is ScreenState.UserChat -> {
                            topScreenHandlerCloseable = wrapWithEffectHandler(
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
                        else -> {
                            Napier.d("topScreenHandlerCloseable = null $eff")
                            topScreenHandlerCloseable = null
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
        wrapWithEffectHandler(
            handler
        )
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
        }
    }
}