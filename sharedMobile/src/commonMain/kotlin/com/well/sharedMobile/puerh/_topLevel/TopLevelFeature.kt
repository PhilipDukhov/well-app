package com.well.sharedMobile.puerh._topLevel

import com.well.serverModels.User
import com.well.serverModels.WebSocketMessage
import com.well.sharedMobile.puerh.call.CallFeature
import com.well.sharedMobile.puerh.login.LoginFeature
import com.well.sharedMobile.puerh.onlineUsers.OnlineUsersFeature
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature.State.*
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature
import com.well.utils.map
import com.well.utils.toSetOf
import com.well.utils.withEmptySet

object TopLevelFeature {
    fun initialState(
    ): State = State(
        tabs = mapOf(Tab.Main to listOf(ScreenState.LaunchScreen)),
        selectedScreenPosition = ScreenPosition(Tab.Main, 0),
    )

    fun initialEffects(): Set<Eff> = setOf(
        Eff.Initial,
    )

    data class State(
        val tabs: Map<Tab, List<ScreenState>>,
        val selectedScreenPosition: ScreenPosition,
    ) {
        val currentScreen = tabs.getValue(selectedScreenPosition.tab)[selectedScreenPosition.index]
        override fun toString() =
            "State($currentScreen at $selectedScreenPosition; tabs=$tabs)"

        fun <T : ScreenState> changeCurrentScreen(block: T.() -> T): State =
            changeScreen(selectedScreenPosition, block)

        fun <T : ScreenState> changeScreen(
            screenPosition: ScreenPosition,
            block: T.() -> T
        ): State = copy(tabs = tabs.copy(screenPosition, block))

        data class ScreenPosition(
            val tab: Tab,
            val index: Int
        )

        enum class Tab {
            Main,
            Overlay,
        }

        sealed class ScreenState(internal val baseState: Any) {
            internal abstract fun baseCopy(state: Any): ScreenState

            object LaunchScreen : ScreenState(Unit) {
                override fun baseCopy(state: Any): ScreenState =
                    throw IllegalStateException()
            }

            data class Login(val state: LoginFeature.State) : ScreenState(state) {
                override fun baseCopy(state: Any): ScreenState =
                    copy(state = state as LoginFeature.State)
            }

            data class MyProfile(val state: MyProfileFeature.State) : ScreenState(state) {
                override fun baseCopy(state: Any): ScreenState =
                    copy(state = state as MyProfileFeature.State)
            }

            data class OnlineUsers(val state: OnlineUsersFeature.State) : ScreenState(state) {
                override fun baseCopy(state: Any): ScreenState =
                    copy(state = state as OnlineUsersFeature.State)
            }

            data class Call(val state: CallFeature.State) : ScreenState(state) {
                override fun baseCopy(state: Any): ScreenState =
                    copy(state = state as CallFeature.State)
            }
        }
    }

    sealed class Msg {
        data class MyProfileMsg(val msg: MyProfileFeature.Msg) : Msg()
        data class OnlineUsersMsg(val msg: OnlineUsersFeature.Msg) : Msg()
        data class CallMsg(val msg: CallFeature.Msg) : Msg()

        data class StartCall(val user: User) : Msg()
        data class IncomingCall(val incomingCall: WebSocketMessage.IncomingCall) : Msg()
        object EndCall : Msg()

        object StopImageSharing : Msg()

        data class ShowAlert(val alert: Alert) : Msg()
        data class OpenUserProfile(val user: User, val isCurrent: Boolean) : Msg()
        object LoggedIn : Msg()
        object OpenLoginScreen : Msg()
        object Back : Msg()
        object Pop : Msg()
    }

    sealed class Eff {
        data class ShowAlert(val alert: Alert) : Eff()
        object SystemBack : Eff()
        object Initial : Eff()
//        data class GotLogInToken(val token: String) : Eff()
        data class MyProfileEff(val eff: MyProfileFeature.Eff) : Eff()
        data class OnlineUsersEff(val eff: OnlineUsersFeature.Eff) : Eff()
        data class CallEff(val eff: CallFeature.Eff) : Eff()
    }

    fun reducer(
        msg: Msg,
        state: State
    ): ReducerResult = run state@{
        return@reducer state toSetOf (run eff@{
            when (msg) {
                is Msg.ShowAlert -> {
                    return@reducer state toSetOf Eff.ShowAlert(msg.alert)
                }
                is Msg.Back -> {
                    when (state.currentScreen) {
                        is ScreenState.OnlineUsers, is ScreenState.Login -> {
                            return@eff Eff.SystemBack
                        }
                        is ScreenState.Call -> {
                            return@reducer state.reduceCall(CallFeature.Msg.Back)
                        }
                        is ScreenState.MyProfile -> {
                            return@reducer state.reduceMyProfile(MyProfileFeature.Msg.Back)
                        }
                        ScreenState.LaunchScreen -> TODO()
                    }
                }
                is Msg.Pop -> {
                    return@state state.copyPop()
                }
                is Msg.OpenUserProfile -> {
                    return@state state.openUserProfile(msg)
                }
                is Msg.LoggedIn -> {
                    return@state state.copy(
                        tabs = mapOf(
                            Tab.Main to listOf(ScreenState.OnlineUsers(OnlineUsersFeature.initialState()))
                        ),
                        selectedScreenPosition = ScreenPosition(Tab.Main, 0),
                    )
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
                is Msg.MyProfileMsg -> {
                    return@reducer state.reduceMyProfile(msg.msg)
                }
                is Msg.OnlineUsersMsg -> {
                    return@reducer state.reduceOnlineUsers(msg.msg)
                }
                is Msg.CallMsg -> {
                    return@reducer state.reduceCall(msg.msg)
                }
                is Msg.StopImageSharing -> {
                    return@state state.copyPop(Tab.Overlay)
                }
                is Msg.EndCall -> {
                    return@state state.copyHideOverlay()
                }
                Msg.OpenLoginScreen -> {
                    return@state state.copyReplace(screen = ScreenState.Login(LoginFeature.State()))
                }
            }
        })
    }.withEmptySet()

    private fun State.openUserProfile(msg: Msg.OpenUserProfile): State =
        copyPush(
            Tab.Main,
            ScreenState.MyProfile(MyProfileFeature.initialState(msg.isCurrent, msg.user))
        )

    private fun State.copyShowCall(callState: CallFeature.State): State =
        copyPush(
            Tab.Overlay,
            ScreenState.Call(callState),
        )

    private fun State.copyHideOverlay(): State =
        copyHideTab(
            Tab.Overlay
        )

    // ScreenState reducers

    private fun State.reduceMyProfile(
        msg: MyProfileFeature.Msg,
    ): ReducerResult =
        reduceScreen<
            ScreenState.MyProfile,
            MyProfileFeature.Msg,
            MyProfileFeature.State,
            MyProfileFeature.Eff
            >(
            msg,
            MyProfileFeature::reducer,
            Eff::MyProfileEff
        )

    private fun State.reduceOnlineUsers(
        msg: OnlineUsersFeature.Msg,
    ): ReducerResult =
        reduceScreen<
            ScreenState.OnlineUsers,
            OnlineUsersFeature.Msg,
            OnlineUsersFeature.State,
            OnlineUsersFeature.Eff
            >(
            msg,
            OnlineUsersFeature::reducer,
            Eff::OnlineUsersEff
        )

    private fun State.reduceCall(
        msg: CallFeature.Msg,
    ): ReducerResult =
        reduceScreen<
            ScreenState.Call,
            CallFeature.Msg,
            CallFeature.State,
            CallFeature.Eff
            >(
            msg,
            CallFeature::reducer,
            Eff::CallEff
        )
}
