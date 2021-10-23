package com.well.sharedMobile.featureProvider

import com.well.modules.atomic.AtomicCloseableLateInitRef
import com.well.modules.atomic.AtomicCloseableRef
import com.well.modules.atomic.AtomicLateInitRef
import com.well.modules.atomic.Closeable
import com.well.modules.atomic.CloseableContainer
import com.well.modules.atomic.asCloseable
import com.well.modules.atomic.freeze
import com.well.modules.db.chatMessages.ChatMessagesDatabase
import com.well.modules.db.chatMessages.insert
import com.well.modules.db.mobile.DatabaseManager
import com.well.modules.db.users.UsersDatabase
import com.well.modules.db.users.getByIdFlow
import com.well.modules.db.users.insertOrReplace
import com.well.modules.models.WebSocketMsg
import com.well.modules.models.chat.ChatMessage
import com.well.modules.utils.AppContext
import com.well.modules.utils.dataStore.AuthInfo
import com.well.modules.utils.dataStore.authInfo
import com.well.modules.utils.dataStore.welcomeShowed
import com.well.modules.utils.permissionsHandler.PermissionsHandler
import com.well.modules.utils.platform.Platform
import com.well.modules.utils.platform.isDebug
import com.well.modules.utils.puerh.ExecutorEffectHandler
import com.well.modules.utils.puerh.ExecutorEffectsInterpreter
import com.well.modules.utils.puerh.SyncFeature
import com.well.modules.utils.puerh.adapt
import com.well.modules.utils.puerh.wrapWithEffectHandler
import com.well.modules.networking.NetworkManager
import com.well.sharedMobile.Alert
import com.well.sharedMobile.ContextHelper
import com.well.sharedMobile.ScreenState
import com.well.sharedMobile.TopLevelFeature
import com.well.sharedMobile.TopLevelFeature.Eff
import com.well.sharedMobile.TopLevelFeature.Msg
import com.well.sharedMobile.WebAuthenticator
import com.well.modules.features.call.webRtc.WebRtcManagerI
import com.well.modules.features.experts.ExpertsFeature
import com.well.modules.features.login.LoginFeature
import com.well.modules.features.login.SocialNetwork
import com.well.modules.features.login.SocialNetworkService
import com.well.modules.features.login.credentialProviders.CredentialProvider
import com.well.modules.features.login.credentialProviders.OAuthCredentialProvider
import com.well.modules.features.more.MoreFeature
import com.well.modules.features.more.about.AboutFeature
import com.well.modules.features.more.support.SupportFeature
import com.well.modules.features.myProfile.MyProfileFeature
import com.well.modules.features.userChat.UserChatEffHandler
import com.well.modules.features.userChat.UserChatFeature
import com.well.modules.features.welcome.WelcomeFeature
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FeatureProvider(
    val appContext: AppContext,
    internal val webRtcManagerGenerator: (List<String>, WebRtcManagerI.Listener) -> WebRtcManagerI,
    providerGenerator: (SocialNetwork, AppContext, WebAuthenticator) -> CredentialProvider,
) {
    internal val coroutineContext = Dispatchers.Default
    internal var sessionInfo by AtomicCloseableRef<SessionInfo>()
    internal val socialNetworkService = SocialNetworkService { network ->
        when (network) {
            SocialNetwork.Twitter -> {
                OAuthCredentialProvider("twitter", contextHelper)
            }
            else -> providerGenerator(network, appContext, contextHelper)
        }
    }
    internal val platform = Platform(appContext)
    internal val permissionsHandler = PermissionsHandler(appContext.permissionsHandlerContext)
    internal val coroutineScope = CoroutineScope(coroutineContext)
    internal val contextHelper = ContextHelper(appContext)
    internal var networkManager by AtomicCloseableLateInitRef<NetworkManager>()
    internal val nonInitializedAuthInfo = AtomicLateInitRef<AuthInfo>()
    internal val callCloseableContainer = CloseableContainer()
    private var topScreenHandlerCloseable by AtomicCloseableRef<Closeable>()
    internal val databaseManager = DatabaseManager(appContext)
    internal val usersDatabase: UsersDatabase
        get() = databaseManager.usersDatabase
    internal val messagesDatabase: ChatMessagesDatabase
        get() = databaseManager.messagesDatabase

    private val effectInterpreter: ExecutorEffectsInterpreter<Eff, Msg> =
        interpreter@{ eff, listener ->
            when (eff) {
                is Eff.ChatListEff,
                -> Unit
                is Eff.ShowAlert -> {
                    MainScope().launch {
                        if (eff.alert is Alert.Throwable) {
                            Napier.e("", eff.alert.throwable)
                        }
                        contextHelper.showAlert(eff.alert)
                    }
                    if (eff.alert is Alert.Throwable) {
                        Napier.e("", eff.alert.throwable)
                        if (!Platform.isDebug) {
                            println("alert! ${eff.alert.throwable}")
                        }
                    }
                }
                is Eff.MyProfileEff -> handleMyProfileEff(eff.eff, listener)
                is Eff.ExpertsEff -> when (eff.eff) {
                    is ExpertsFeature.Eff.UpdateList,
                    is ExpertsFeature.Eff.SetUserFavorite,
                    is ExpertsFeature.Eff.FilterEff,
                    -> Unit
                    is ExpertsFeature.Eff.SelectedUser -> {
                        listener.invoke(
                            Msg.Push(
                                ScreenState.MyProfile(
                                    state = MyProfileFeature.initialState(
                                        isCurrent = sessionInfo!!.uid == eff.eff.user.id,
                                        user = eff.eff.user,
                                    )
                                )
                            )
                        )
                    }
                    is ExpertsFeature.Eff.CallUser -> {
                        handleCall(eff.eff.user, listener)
                    }
                }
                is Eff.CallEff -> handleCallEff(eff.eff, listener)
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
                Eff.SystemBack -> {
                    appContext.systemBack()
                }
                is Eff.LoginEff -> {
                    when (eff.eff) {
                        is LoginFeature.Eff.Login -> {
                            socialNetworkLogin(eff.eff.socialNetwork, listener)
                        }
                    }
                }
                is Eff.WelcomeEff -> {
                    when (eff.eff) {
                        WelcomeFeature.Eff.Continue -> {
//                            platform.dataStore.welcomeShowed = true
                            listener.invoke(Msg.OpenLoginScreen)
                        }
                    }
                }
                is Eff.AboutEff -> {
                    when (eff.eff) {
                        AboutFeature.Eff.Back -> {
                            listener(Msg.Pop)
                        }
                        is AboutFeature.Eff.OpenLink -> {
                            contextHelper.openUrl(eff.eff.link)
                        }
                        is AboutFeature.Eff.Push -> {
                            error("${eff.eff} should be handler in screen state")
                        }
                    }
                }
                is Eff.MoreEff -> {
                    when (eff.eff) {
                        is MoreFeature.Eff.Push -> {
                            error("${eff.eff} should be handler in screen state")
                        }
                    }
                }
                is Eff.SupportEff -> {
                    when (eff.eff) {
                        SupportFeature.Eff.Back -> {
                            listener(Msg.Pop)
                        }
                        is SupportFeature.Eff.Send -> {
                            listener(Msg.Pop)
                        }
                    }
                }
                is Eff.UserChatEff -> {
                    when (eff.eff) {
                        UserChatFeature.Eff.ChooseImage,
                        is UserChatFeature.Eff.MarkMessageRead,
                        is UserChatFeature.Eff.SendImage,
                        is UserChatFeature.Eff.SendMessage,
                        -> Unit
                        UserChatFeature.Eff.Back -> {
                            listener(Msg.Pop)
                        }
                        is UserChatFeature.Eff.Call -> {
                            listener(Msg.StartCall(eff.eff.user))
                        }
                        is UserChatFeature.Eff.OpenUserProfile -> {
                            listener(
                                Msg.Push(
                                    ScreenState.MyProfile(
                                        state = MyProfileFeature.initialState(
                                            isCurrent = false,
                                            user = eff.eff.user,
                                        )
                                    )
                                )
                            )
                        }
                    }
                }
                is Eff.TopScreenUpdated -> {
                    when (eff.screen) {
                        is ScreenState.MyProfile -> {
                            if (eff.screen.state.user?.initialized != false) {
                                topScreenHandlerCloseable =
                                    coroutineScope.launch {
                                        usersDatabase.usersQueries
                                            .getByIdFlow(eff.screen.state.uid)
                                            .collect { user ->
                                                listener(
                                                    Msg.MyProfileMsg(
                                                        MyProfileFeature.Msg.RemoteUpdateUser(user)
                                                    )
                                                )
                                            }
                                    }.asCloseable()
                            }
                        }
                        is ScreenState.UserChat -> {
                            val peerId = eff.screen.state.peerId
                            topScreenHandlerCloseable = feature.wrapWithEffectHandler(
                                UserChatEffHandler(
                                    currentUid = sessionInfo!!.uid,
                                    peerUid = peerId,
                                    services = UserChatEffHandler.Services(
                                        createChatMessage = {
                                            networkManager.send(
                                                WebSocketMsg.Front.CreateChatMessage(
                                                    it
                                                )
                                            )
                                        },
                                        uploadMessagePicture = networkManager::uploadMessagePicture,
                                        peerUserFlow = {
                                            usersDatabase.usersQueries.getByIdFlow(
                                                peerId
                                            )
                                        },
                                        pickSystemImage = contextHelper::pickSystemImage,
                                        cacheImage = contextHelper.appContext::cacheImage,
                                    ),
                                    messagesDatabase = messagesDatabase,
                                    coroutineScope = CoroutineScope(coroutineContext),
                                ).adapt(
                                    effAdapter = { (it as? Eff.UserChatEff)?.eff },
                                    msgAdapter = { Msg.UserChatMsg(it) }
                                )
                            )
                        }
                        else -> Unit
                    }
                }
            }
        }

    val feature = SyncFeature(
        TopLevelFeature.initialState(),
        TopLevelFeature.initialEffects(),
        TopLevelFeature::reducer,
        coroutineContext
    )

    init {
        // preventing iOS InvalidMutabilityException
        val handler = ExecutorEffectHandler(
            effectInterpreter,
            coroutineScope,
        )
        feature.wrapWithEffectHandler(
            handler
        )
        handler.freeze()
    }

    internal fun webSocketMessageHandler(
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
                usersDatabase.usersQueries.run {
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
                messagesDatabase.chatMessagesQueries.run {
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
        }
    }
}