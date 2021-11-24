package com.well.sharedMobile

import com.well.modules.annotations.ScreenStates
import com.well.modules.features.call.callFeature.CallFeature
import com.well.modules.features.chatList.chatListFeature.ChatListFeature
import com.well.modules.features.experts.expertsFeature.ExpertsFeature
import com.well.modules.features.login.loginFeature.LoginFeature
import com.well.modules.features.more.MoreFeature
import com.well.modules.features.more.about.AboutFeature
import com.well.modules.features.more.support.SupportFeature
import com.well.modules.features.myProfile.myProfileFeature.MyProfileFeature
import com.well.modules.features.userChat.userChatFeature.UserChatFeature
import com.well.modules.features.welcome.WelcomeFeature
import com.well.modules.models.User
import com.well.modules.models.WebSocketMsg
import com.well.modules.puerhBase.toSetOf
import com.well.modules.puerhBase.withEmptySet
import com.well.modules.utils.kotlinUtils.map
import com.well.modules.utils.kotlinUtils.mapSecond
import com.well.modules.utils.kotlinUtils.spacedUppercaseName
import com.well.modules.utils.viewUtils.Alert
import com.well.sharedMobile.TopLevelFeature.State.ScreenPosition
import com.well.sharedMobile.TopLevelFeature.State.Tab
import com.well.sharedMobile.featureProvider.loggedInTabs

// to remove build/tmp/kapt3 after updating features to refresh cache
// run kaptClean&Generate config
@ScreenStates(
    empties = [
        "Launch"
    ],
    features = [
        WelcomeFeature::class,
        LoginFeature::class,
        MyProfileFeature::class,
        ExpertsFeature::class,
        CallFeature::class,
        MoreFeature::class,
        AboutFeature::class,
        SupportFeature::class,
        ChatListFeature::class,
        UserChatFeature::class,
    ]
)
object TopLevelFeature {
    fun initialState(
    ): State = State(
        tabs = mapOf(Tab.Login to listOf(ScreenState.Launch)),
        selectedScreenPosition = ScreenPosition(Tab.Login, 0),
    )

    data class State internal constructor(
        internal val tabs: Map<Tab, List<ScreenState>>,
        internal val selectedScreenPosition: ScreenPosition,
    ) {
        @Suppress("unused")
        val backButtonNeeded = selectedScreenPosition.index > 0
        val currentScreen = run {
            if (listOf(
                    Tab.Overlay,
                    Tab.Login
                ).contains(selectedScreenPosition.tab) || selectedScreenPosition.index > 0
            ) {
                Screen.Single(
                    tabs.getValue(selectedScreenPosition.tab)[selectedScreenPosition.index]
                )
            } else {
                Screen.Tabs(
                    tabs.map { Screen.Tabs.TabScreen(it.key, it.value.last()) }
                )
            }
        }
        val selectedTab = selectedScreenPosition.tab
        val topScreen = when (currentScreen) {
            is Screen.Single -> currentScreen.screen
            is Screen.Tabs -> currentScreen.tabs.first { it.tab == selectedTab }.screen
        }

        sealed interface Screen {
            data class Tabs(val tabs: List<TabScreen>) : Screen {
                operator fun get(tab: Tab): TabScreen = tabs.first { it.tab == tab }

                data class TabScreen(val tab: Tab, val screen: ScreenState)
            }

            data class Single(val screen: ScreenState) : Screen
        }

        override fun toString() =
            "State($currentScreen at $selectedScreenPosition; tabs=$tabs)"

        internal fun <T : ScreenState> changeScreen(
            screenPosition: ScreenPosition,
            block: T.() -> T,
        ): State = copy(tabs = tabs.copy(screenPosition = screenPosition, block = block))

        internal data class ScreenPosition(
            val tab: Tab,
            val index: Int,
        )

        enum class Tab {
            Login,

            MyProfile,
            Experts,
            ChatList,
            More,

            Overlay,
            ;

            fun spacedName() = spacedUppercaseName()

            fun isTabBar() = when (this) {
                MyProfile,
                Experts,
                ChatList,
                More,
                -> true
                else -> false
            }
        }
    }

    sealed class Msg {
        object Initial : Msg()
        data class StartCall(val user: User) : Msg()
        data class IncomingCall(val incomingCall: WebSocketMsg.Back.IncomingCall) : Msg()
        object EndCall : Msg()

        object StopImageSharing : Msg()

        data class ShowAlert(val alert: Alert) : Msg()
        data class LoggedIn(val uid: User.Id) : Msg()
        data class PushMyProfile(val myProfileState: MyProfileFeature.State) : Msg()
        data class SelectTab(val tab: Tab) : Msg()
        object OpenLoginScreen : Msg()
        object OpenWelcomeScreen : Msg()
        data class OpenUserChat(val uid: User.Id) : Msg()
        object Back : Msg()
        object Pop : Msg()
    }

    internal sealed interface Eff {
        data class ShowAlert(val alert: Alert) : Eff
        object SystemBack : Eff
        object Initial : Eff
        object InitialLoggedIn : Eff
        data class TopScreenAppeared(val screen: ScreenState, val position: ScreenPosition) : Eff
    }

    internal fun reducer(
        msg: Msg,
        state: State,
    ): ReducerResult = run state@{
        return@reducer state toSetOf (run eff@{
            when (msg) {
                is Msg.Initial -> {
                    return@eff Eff.Initial
                }
                is Msg.ShowAlert -> {
                    return@reducer state toSetOf Eff.ShowAlert(msg.alert)
                }
                is Msg.Back -> {
                    val childBackReducerResult = reduceBackMsg(state)
                    if (childBackReducerResult != null) {
                        return@reducer childBackReducerResult
                    } else {
                        return@reducer state.reducePop()
                    }
                }
                is Msg.Pop -> {
                    return@reducer state.reducePop()
                }
                is Msg.PushMyProfile -> {
                    return@reducer state.copyPush(
                        state = msg.myProfileState,
                        createScreen = ScreenState::MyProfile,
                    ).reduceScreenChanged()
                }
                is Msg.SelectTab -> {
                    return@reducer if (state.selectedScreenPosition.tab == msg.tab)
                        state.copyPopToRoot().reduceScreenChanged()
                    else
                        state.copy(
                            selectedScreenPosition = ScreenPosition(
                                tab = msg.tab,
                                index = state.tabs[msg.tab]!!.indices.last
                            ),
                        ).reduceScreenChanged()
                }
                is Msg.LoggedIn -> {
                    val newState = state.copy(
                        tabs = loggedInTabs(msg.uid),
                        selectedScreenPosition = ScreenPosition(Tab.Experts, 0),
                    )
                    return@reducer newState
                        .reduceScreenChanged()
                        .mapSecond { it + Eff.InitialLoggedIn }
                }
                is Msg.IncomingCall -> {
                    val (callState, effs) = CallFeature.incomingStateAndEffects(msg.incomingCall)
                    return@reducer state.copyShowCall(callState) to
                            effs.mapTo(
                                featureEff = FeatureEff::Call,
                                position = ScreenPosition(tab = Tab.Overlay, index = 0)
                            )
                }
                is Msg.StartCall -> {
                    return@reducer CallFeature.callingStateAndEffects(msg.user)
                        .map(
                            { state.copyShowCall(it) },
                            {
                                it.mapTo(
                                    featureEff = FeatureEff::Call,
                                    position = ScreenPosition(tab = Tab.Overlay, index = 0)
                                )
                            },
                        )
                }
                is FeatureMsg -> {
                    return@reducer reduceScreenMsg(msg, state)
                }
                is Msg.StopImageSharing -> {
                    return@reducer state.copyPop(Tab.Overlay).reduceScreenChanged()
                }
                is Msg.EndCall -> {
                    return@reducer state.copyHideOverlay().reduceScreenChanged()
                }
                Msg.OpenLoginScreen -> {
                    return@state state.copy(
                        tabs = mapOf(
                            createTab(Tab.Login, LoginFeature.State(), ScreenState::Login)
                        ),
                        selectedScreenPosition = ScreenPosition(Tab.Login, 0),
                    )
                }
                Msg.OpenWelcomeScreen -> {
                    return@state state.copyReplace(
                        state = WelcomeFeature.State(),
                        createScreen = ScreenState::Welcome
                    )
                }
                is Msg.OpenUserChat -> {
                    val popNeeded = state.selectedScreenPosition.let { position ->
                        if (position.index == 0) return@let false
                        val chatScreen = state.tabs[position.tab]
                            ?.get(position.index - 1) as? ScreenState.UserChat
                            ?: return@let false
                        chatScreen.state.peerId == msg.uid
                    }
                    val newState = if (popNeeded) {
                        state.copyPop()
                    } else {
                        state.copyPush(
                            state = UserChatFeature.State(
                                peerId = msg.uid,
                                backToUser = state.topScreen is ScreenState.UserChat,
                            ),
                            createScreen = ScreenState::UserChat,
                        )
                    }
                    return@reducer newState.reduceScreenChanged()
                }
            }
        })
    }.withEmptySet()

    private fun State.copyShowCall(callState: CallFeature.State): State =
        copyPush(
            Tab.Overlay,
            callState,
            ScreenState::Call,
        )

    private fun State.copyHideOverlay(): State =
        copyHideTab(
            Tab.Overlay
        )

    private fun State.reducePop() =
        if (selectedTab.isTabBar() && selectedScreenPosition.index == 0) {
            this toSetOf Eff.SystemBack
        } else {
            copyPop().reduceScreenChanged()
        }
}
