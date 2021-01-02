package com.well.sharedMobile.puerh.topLevel

import com.well.serverModels.User
import com.well.serverModels.WebSocketMessage
import com.well.sharedMobile.puerh.call.CallFeature
import com.well.sharedMobile.puerh.login.LoginFeature
import com.well.sharedMobile.puerh.onlineUsers.OnlineUsersFeature
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature.Alert.Action.*
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature.Eff.*
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature.State.*
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature.State.ScreenState.*
import com.well.utils.map

private typealias ReducerResult = Pair<TopLevelFeature.State, Set<TopLevelFeature.Eff>>

inline fun <reified R : ScreenState> Map<Tab, List<ScreenState>>.screenAndPositionOfFirstOrNull(
): Pair<R, ScreenPosition>? {
    for ((tab, screens) in this) {
        for ((index, screen) in screens.withIndex()) {
            if (screen is R) {
                return screen to ScreenPosition(tab, index)
            }
        }
    }
    return null
}

object TopLevelFeature {
    fun initialState(
    ): State = State(
        mapOf(Tab.Main to listOf(Login(LoginFeature.State()))),
        ScreenPosition(Tab.Main, 0),
    )

    fun initialEffects(): Set<Eff> = setOf(
        TestLogin
    )

    data class State(
        val tabs: Map<Tab, List<ScreenState>>,
        val selectedScreenPosition: ScreenPosition,
    ) {
        val currentScreen = tabs.getValue(selectedScreenPosition.tab)[selectedScreenPosition.index]
        override fun toString() =
//            try {
//                currentScreen
//            } catch (t: Throwable) {
//                "currentScreen empty: $t"
//            }.let {
            "State(it at $selectedScreenPosition; tabs=$tabs)"
//            }

        fun <T : ScreenState> changeCurrentScreen(block: T.() -> T): State =
            changeScreen(selectedScreenPosition, block)

        fun <T : ScreenState> changeScreen(
            screenPosition: ScreenPosition,
            block: T.() -> T
        ): State =
            copy(tabs = tabs.copy(screenPosition, block))

        private fun <T : ScreenState> Map<Tab, List<ScreenState>>.copy(
            screenPosition: ScreenPosition,
            block: T.() -> T,
        ): Map<Tab, List<ScreenState>> =
            toMutableMap().also { mutableTabs ->
                mutableTabs[screenPosition.tab] = mutableTabs
                    .getValue(screenPosition.tab)
                    .toMutableList()
                    .also {
                        @Suppress("UNCHECKED_CAST")
                        it[screenPosition.index] = block(it[screenPosition.index] as T)
                    }
            }

        data class ScreenPosition(
            val tab: Tab,
            val index: Int
        )

        enum class Tab {
            Main,
            Overlay,
        }

        sealed class ScreenState {
            data class OnlineUsers(val state: OnlineUsersFeature.State) : ScreenState()
            data class Call(val state: CallFeature.State) : ScreenState()
            data class Login(val state: LoginFeature.State) : ScreenState()
        }
    }

    private const val callDenied =
        "Well needs access to your microphone and camera so that you can make video calls"

    sealed class Alert(
        val description: String,
        val positiveAction: Action,
        val negativeAction: Action,
    ) {
        object CameraOrMicDenied : Alert(callDenied, Ok, Settings)

        sealed class Action(val title: String) {
            object Ok : Action("OK")
            object Settings : Action("Settings")
        }
    }

    sealed class Msg {
        data class OnlineUsersMsg(val msg: OnlineUsersFeature.Msg) : Msg()
        data class CallMsg(val msg: CallFeature.Msg) : Msg()

        data class StartCall(val user: User) : Msg()
        data class IncomingCall(val incomingCall: WebSocketMessage.IncomingCall) : Msg()
        object EndCall : Msg()

        data class ShowAlert(val alert: Alert) : Msg()
        object LoggedIn : Msg()
    }

    sealed class Eff {
        data class ShowAlert(val alert: Alert) : Eff()
        object TestLogin : Eff()
        data class GotLogInToken(val token: String) : Eff()
        data class OnlineUsersEff(val eff: OnlineUsersFeature.Eff) : Eff()
        data class CallEff(val eff: CallFeature.Eff) : Eff()
    }

    fun reducer(
        msg: Msg,
        state: State
    ): ReducerResult {
        println("Top level reducer: $msg")
        return when (msg) {
            is Msg.ShowAlert -> state to setOf(ShowAlert(msg.alert))
            is Msg.IncomingCall -> {
                state.copyShowCall(
                    CallFeature.incomingInitialState(msg.incomingCall)
                ) to setOf()
            }
            is Msg.OnlineUsersMsg -> {
                reduceOnlineUsers(
                    state.tabs
                        .screenAndPositionOfFirstOrNull()
                        ?: throw IllegalStateException("$msg | $state"),
                    msg.msg,
                    state
                )
            }
            else -> when (state.currentScreen) {
                is OnlineUsers -> when (msg) {
                    is Msg.StartCall ->
                        CallFeature.callInitiateStateAndEffects(msg.user)
                            .map(
                                { state.copyShowCall(it) },
                                {
                                    it.map(::CallEff)
                                        .toSet()
                                },
                            )
                    else -> throw IllegalStateException("$msg $state")
                }
                is Call -> when (msg) {
                    is Msg.CallMsg -> reduceCall(state.currentScreen, msg.msg, state)
                    is Msg.EndCall ->
                        state.copyHideOverlay() to setOf()
                    else -> throw IllegalStateException("$msg $state")
                }
                is Login -> when (msg) {
                    Msg.LoggedIn -> {
                        state.copyLogin() to setOf()
                    }
                    else -> throw IllegalStateException("$msg $state")
                }
            }
        }
    }

    private fun State.copyLogin(): State =
        copy(
            tabs = mapOf(
                Tab.Main to listOf(OnlineUsers(OnlineUsersFeature.initialState()))
            ),
            selectedScreenPosition = ScreenPosition(Tab.Main, 0),
        )

    private fun State.copyShowCall(callState: CallFeature.State): State =
        copy(
            tabs = tabs
                .toMutableMap()
                .also { mutableTabs ->
                    mutableTabs[Tab.Overlay] = listOf(Call(callState))
                },
            selectedScreenPosition = ScreenPosition(Tab.Overlay, 0),
        )

    private fun State.copyHideOverlay() =
        copy(
            tabs = tabs
                .toMutableMap()
                .also { mutableTabs ->
                    mutableTabs.remove(Tab.Overlay)
                },
            selectedScreenPosition = ScreenPosition(
                Tab.Main,
                tabs[Tab.Main]?.lastIndex
                    ?: throw IllegalStateException("copyHideOverlay: main tab is unexpectedly empty")
            ),
        )

    private fun reduceOnlineUsers(
        screenAndPosition: Pair<OnlineUsers, ScreenPosition>,
        msg: OnlineUsersFeature.Msg,
        state: State
    ): ReducerResult {
        val (newScreenState, effs) = OnlineUsersFeature.reducer(msg, screenAndPosition.first.state)
        val newEffs = effs.mapTo(HashSet(), Eff::OnlineUsersEff)
        return state.changeScreen<OnlineUsers>(screenAndPosition.second) {
            copy(state = newScreenState)
        } to newEffs
    }

    private fun reduceCall(
        currentScreen: Call,
        msg: CallFeature.Msg,
        state: State
    ): ReducerResult {
        val (newScreenState, effs) = CallFeature.reducer(msg, currentScreen.state)
        val newEffs = effs.mapTo(HashSet(), Eff::CallEff)
        return state.changeCurrentScreen<Call> { copy(state = newScreenState) } to newEffs
    }
}
