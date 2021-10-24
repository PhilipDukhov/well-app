package com.well.sharedMobile

import com.well.modules.annotations.ScreenStates
import com.well.modules.models.User
import com.well.modules.models.UserId
import com.well.modules.models.WebSocketMsg
import com.well.modules.models.spacedUppercaseName
import com.well.modules.utils.map
import com.well.modules.utils.toSetOf
import com.well.modules.utils.withEmptySet
import com.well.sharedMobile.TopLevelFeature.State.*
import com.well.modules.features.call.CallFeature
import com.well.modules.features.chatList.ChatListFeature
import com.well.modules.features.experts.ExpertsFeature
import com.well.modules.features.login.LoginFeature
import com.well.modules.features.more.MoreFeature
import com.well.modules.features.more.about.AboutFeature
import com.well.modules.features.more.support.SupportFeature
import com.well.modules.features.myProfile.MyProfileFeature
import com.well.modules.features.userChat.UserChatFeature
import com.well.modules.features.welcome.WelcomeFeature
import com.well.modules.viewHelpers.Alert

// remove build/tmp/kapt3 after updating features to refresh cache
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

    internal fun initialEffects(): Set<Eff> = setOf(
        Eff.Initial,
    )

    data class State(
        internal val tabs: Map<Tab, List<ScreenState>>,
        internal val selectedScreenPosition: ScreenPosition,
    ) {
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

        sealed class Screen {
            data class Tabs(val tabs: List<TabScreen>) : Screen() {
                operator fun get(tab: Tab): TabScreen = tabs.first { it.tab == tab }

                data class TabScreen(val tab: Tab, val screen: ScreenState)
            }

            data class Single(val screen: ScreenState) : Screen()
        }

        override fun toString() =
            "State($currentScreen at $selectedScreenPosition; tabs=$tabs)"

        fun <T : ScreenState> changeScreen(
            screenPosition: ScreenPosition,
            block: T.() -> T,
        ): State = copy(tabs = tabs.copy(screenPosition = screenPosition, block = block))

        data class ScreenPosition(
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
        data class CallMsg(val msg: CallFeature.Msg) : Msg()
        data class WelcomeMsg(val msg: WelcomeFeature.Msg) : Msg()
        data class MyProfileMsg(val msg: MyProfileFeature.Msg) : Msg()
        data class LoginMsg(val msg: LoginFeature.Msg) : Msg()
        data class ExpertsMsg(val msg: ExpertsFeature.Msg) : Msg()
        data class ChatListMsg(val msg: ChatListFeature.Msg) : Msg()
        data class MoreMsg(val msg: MoreFeature.Msg) : Msg()
        data class AboutMsg(val msg: AboutFeature.Msg) : Msg()
        data class SupportMsg(val msg: SupportFeature.Msg) : Msg()
        data class UserChatMsg(val msg: UserChatFeature.Msg) : Msg()

        data class StartCall(val user: User) : Msg()
        data class IncomingCall(val incomingCall: WebSocketMsg.Back.IncomingCall) : Msg()
        object EndCall : Msg()

        object StopImageSharing : Msg()

        data class ShowAlert(val alert: Alert) : Msg()
        data class LoggedIn(val uid: UserId) : Msg()
        data class Push(val screen: ScreenState) : Msg()
        data class SelectTab(val tab: Tab) : Msg()
        object OpenLoginScreen : Msg()
        object OpenWelcomeScreen : Msg()
        data class OpenUserChat(val uid: UserId) : Msg()
        object Back : Msg()
        object Pop : Msg()
    }

    internal sealed class Eff {
        data class ShowAlert(val alert: Alert) : Eff()
        object SystemBack : Eff()
        object Initial : Eff()
        data class TopScreenUpdated(val screen: ScreenState) : Eff()

        data class CallEff(val eff: CallFeature.Eff) : Eff()
        data class MyProfileEff(val eff: MyProfileFeature.Eff) : Eff()
        data class WelcomeEff(val eff: WelcomeFeature.Eff) : Eff()
        data class LoginEff(val eff: LoginFeature.Eff) : Eff()
        data class ExpertsEff(val eff: ExpertsFeature.Eff) : Eff()
        data class ChatListEff(val eff: ChatListFeature.Eff) : Eff()
        data class UserChatEff(val eff: UserChatFeature.Eff) : Eff()
        data class MoreEff(val eff: MoreFeature.Eff) : Eff()
        data class AboutEff(val eff: AboutFeature.Eff) : Eff()
        data class SupportEff(val eff: SupportFeature.Eff) : Eff()
    }

    internal fun reducer(
        msg: Msg,
        state: State,
    ): ReducerResult = run state@{
        return@reducer state toSetOf (run eff@{
            when (msg) {
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
                is Msg.Push -> {
                    return@reducer state.copyPush(screen = msg.screen).reduceScreenChanged()
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
                        tabs = mapOf(
                            Tab.MyProfile to listOf(
                                ScreenState.MyProfile(
                                    MyProfileFeature.initialState(
                                        isCurrent = true, msg.uid
                                    )
                                )
                            ),
                            Tab.Experts to listOf(ScreenState.Experts(ExpertsFeature.initialState())),
                            Tab.ChatList to listOf(ScreenState.ChatList(ChatListFeature.State())),
                            Tab.More to listOf(ScreenState.More(MoreFeature.State())),
                        ),
                        selectedScreenPosition = ScreenPosition(Tab.Experts, 0),
                    )
                    return@reducer newState.reduceScreenChanged()
                }
                is Msg.IncomingCall -> {
                    val (callState, effs) = CallFeature.incomingStateAndEffects(msg.incomingCall)
                    return@reducer state.copyShowCall(callState) to
                            effs.mapTo(HashSet(), Eff::CallEff)
                }
                is Msg.StartCall -> {
                    return@reducer CallFeature.callingStateAndEffects(msg.user)
                        .map(
                            { state.copyShowCall(it) },
                            {
                                it.map(Eff::CallEff)
                                    .toSet()
                            },
                        )
                }
                is Msg.WelcomeMsg,
                is Msg.LoginMsg,
                is Msg.MyProfileMsg,
                is Msg.ExpertsMsg,
                is Msg.CallMsg,
                is Msg.MoreMsg,
                is Msg.AboutMsg,
                is Msg.SupportMsg,
                is Msg.ChatListMsg,
                is Msg.UserChatMsg,
                -> {
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
                            Tab.Login to listOf(
                                ScreenState.Login(LoginFeature.State())
                            ),
                        ),
                        selectedScreenPosition = ScreenPosition(Tab.Login, 0),
                    )
                }
                Msg.OpenWelcomeScreen -> {
                    return@state state.copyReplace(screen = ScreenState.Welcome(WelcomeFeature.State()))
                }
                is Msg.OpenUserChat -> {
                    return@reducer state
                        .copyPush(
                            screen = ScreenState.UserChat(
                                UserChatFeature.State(
                                    peerId = msg.uid,
                                    backToUser = state.topScreen is ScreenState.UserChat,
                                )
                            )
                        )
                        .reduceScreenChanged()
                }
            }
        })
    }.withEmptySet()

    private fun State.copyShowCall(callState: CallFeature.State): State =
        copyPush(
            Tab.Overlay,
            ScreenState.Call(callState),
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