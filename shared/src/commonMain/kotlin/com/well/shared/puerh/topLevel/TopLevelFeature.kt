package com.well.shared.puerh.topLevel

import com.github.aakira.napier.Napier
import com.well.serverModels.User
import com.well.serverModels.WebSocketMessage
import com.well.shared.puerh.call.CallFeature
import com.well.shared.puerh.login.LoginFeature
import com.well.shared.puerh.onlineUsers.OnlineUsersFeature
import com.well.shared.puerh.topLevel.TopLevelFeature.Alert.Action.*
import com.well.shared.puerh.topLevel.TopLevelFeature.Eff.*
import com.well.shared.puerh.topLevel.TopLevelFeature.State.*
import com.well.shared.puerh.topLevel.TopLevelFeature.State.ScreenState.OnlineUsers
import com.well.utils.map
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

private typealias ReducerResult = Pair<TopLevelFeature.State, Set<TopLevelFeature.Eff>>

inline fun <reified R: ScreenState> Map<Tab, List<ScreenState>>.screenAndPositionOfFirstOrNull(
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

enum class TestDevice {
    Emulator,
    Simulator,
    Iphone,
    Android;

    object Tokens {
        val facebookMichael =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJBdXRoZW50aWNhdGlvbiIsImlzcyI6Imt0b3IuaW8iLCJpZCI6MzZ9.81VSc4fG-ibZYfbXkEkuolYOxe0RZKBU4BUTo36xHrDFIlymAz2RSznxlh3pz5bVGquEDQbO3ZwjD5OeAR8fKQ"
        val facebookMadison =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJBdXRoZW50aWNhdGlvbiIsImlzcyI6Imt0b3IuaW8iLCJpZCI6NH0.jItmjOB4ofKD94HhZ1UOrYJ6JpunfNSJLjJ1F5y4lBT1YCpC2y3rQTMOSFcancI4s0fRJjI6AIfnbQUeu2ZpcA"
        val facebookPhil =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJBdXRoZW50aWNhdGlvbiIsImlzcyI6Imt0b3IuaW8iLCJpZCI6Mn0.IE6mMsjdWMzkuBnEHqI9LcS67C8BT7O_Ooe4KzRCULFtLwduhbDy7-e0VMOZEwZtbWJV8MbFMYfkZ1FQj0np6A"
        val googleMiinstradamus =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJBdXRoZW50aWNhdGlvbiIsImlzcyI6Imt0b3IuaW8iLCJpZCI6M30.prOmXoRUj22v6VYwX6iDLIZc60J706T8GXxvYGhHj86kGoS7mjiZ36f5EKft-J3u8TK83YfDz9E5W_GPFhzRMA"
    }

    val token: String
        get() = when (this) {
            Emulator -> Tokens.facebookMadison
            Simulator -> Tokens.facebookMichael
            Iphone -> Tokens.facebookPhil
            Android -> Tokens.googleMiinstradamus
        }
}

object TopLevelFeature {
    fun initialState(
    ): State = State(
        mapOf(Tab.Main to listOf(ScreenState.Login(LoginFeature.State()))),
        ScreenPosition(Tab.Main, 0),
    )

    fun initialEffects(testDevice: TestDevice): Set<Eff> = setOf(
        GotLogInToken(testDevice.token)
    )

    data class State(
        val tabs: Map<Tab, List<ScreenState>>,
        val selectedScreenPosition: ScreenPosition,
    ) {
        val currentScreen = tabs.getValue(selectedScreenPosition.tab)[selectedScreenPosition.index]

        fun <T : ScreenState> changeCurrentScreen(block: T.() -> T): State =
            changeScreen(selectedScreenPosition, block)

        fun <T : ScreenState> changeScreen(screenPosition: ScreenPosition, block: T.() -> T): State =
            copy(tabs = tabs.copy(screenPosition, block))

        private fun <T: ScreenState> Map<Tab, List<ScreenState>>.copy(
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

        data class ScreenPosition(val tab: Tab, val index: Int)
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
        data class GotLogInToken(val token: String) : Eff()
        data class OnlineUsersEff(val eff: OnlineUsersFeature.Eff) : Eff()
        data class CallEff(val eff: CallFeature.Eff) : Eff()
    }

    fun reducer(msg: Msg, state: State): ReducerResult = when (msg) {
        is Msg.ShowAlert -> state to setOf(ShowAlert(msg.alert))
        is Msg.StartCall ->
            CallFeature.callInitiateStateAndEffects(msg.user)
                .map(
                    { state.copyShowCall(it) },
                    { it.map(::CallEff).toSet() },
                )
        is Msg.IncomingCall -> {
            state.copyShowCall(
                CallFeature.incomingInitialState(msg.incomingCall)
            ) to setOf()
        }
        is Msg.OnlineUsersMsg -> {
            reduceOnlineUsers(
                state.tabs
                    .screenAndPositionOfFirstOrNull() ?: error("$msg | $state"),
                msg.msg,
                state
            )
        }
        else -> when (state.currentScreen) {
            is OnlineUsers -> when (msg) {
                else ->
                    throw IllegalStateException("$msg | $state")
            }
            is ScreenState.Call -> when (msg) {
                is Msg.CallMsg -> reduceCall(state.currentScreen, msg.msg, state)
                is Msg.EndCall ->
                    state.copyHideOverlay() to setOf()
                else -> error("$msg | $state")
            }
            is ScreenState.Login -> when (msg) {
                Msg.LoggedIn -> {
                    state.copyLogin() to OnlineUsersFeature.initialEffects()
                        .map(::OnlineUsersEff)
                        .toSet()
                }
                else -> error("$msg | $state")
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
                    mutableTabs[Tab.Overlay] = listOf(ScreenState.Call(callState))
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
                tabs[Tab.Main]?.lastIndex ?: error("copyHideOverlay: main tab is unexpectedly empty")
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
        currentScreen: ScreenState.Call,
        msg: CallFeature.Msg,
        state: State
    ): ReducerResult {
        val (newScreenState, effs) = CallFeature.reducer(msg, currentScreen.state)
        val newEffs = effs.mapTo(HashSet(), Eff::CallEff)
        return state.changeCurrentScreen<ScreenState.Call> { copy(state = newScreenState) } to newEffs
    }
}
