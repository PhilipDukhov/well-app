package com.well.shared.puerh.topLevel

import com.github.aakira.napier.Napier
import com.well.serverModels.User
import com.well.shared.puerh.onlineUsers.OnlineUsersFeature
import com.well.shared.puerh.topLevel.TopLevelFeature.Alert.Action.*
import com.well.shared.puerh.topLevel.TopLevelFeature.Eff.*
import com.well.shared.puerh.topLevel.TopLevelFeature.State.*
import com.well.shared.puerh.topLevel.TopLevelFeature.State.ScreenState.OnlineUsers

private typealias ReducerResult = Pair<TopLevelFeature.State, Set<TopLevelFeature.Eff>>

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
        mapOf(Tab.Main to listOf(ScreenState.Login)),
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
            @Suppress("UNCHECKED_CAST")
            copy(tabs = (currentScreen as? T)?.block()?.let {
                tabs.copy(
                    selectedScreenPosition,
                    it
                )
            } ?: tabs)

        private fun Map<Tab, List<ScreenState>>.copy(
            screenPosition: ScreenPosition,
            newScreen: ScreenState
        ): Map<Tab, List<ScreenState>> =
            toMutableMap().also { mutableTabs ->
                mutableTabs[screenPosition.tab] = mutableTabs
                    .getValue(screenPosition.tab)
                    .toMutableList()
                    .also {
                        it[screenPosition.index] = newScreen
                    }
            }

        data class ScreenPosition(val tab: Tab, val index: Int)
        enum class Tab {
            Main,
            Overlay,
        }

        sealed class ScreenState {
            data class OnlineUsers(val state: OnlineUsersFeature.State) : ScreenState()
            object Call : ScreenState()
            object Login : ScreenState()
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
        data class Call(val user: User) : Msg()
        data class ShowAlert(val alert: Alert) : Msg()
        object LoggedIn : Msg()
    }

    sealed class Eff {
        data class OnlineUsersEff(val eff: OnlineUsersFeature.Eff) : Eff()
        data class GotLogInToken(val token: String) : Eff()
        data class ShowAlert(val alert: Alert) : Eff()
    }

    fun reducer(msg: Msg, state: State): ReducerResult = when (msg) {
        is Msg.ShowAlert -> state to setOf(ShowAlert(msg.alert))
        else -> when (state.currentScreen) {
            is OnlineUsers -> when (msg) {
                is Msg.OnlineUsersMsg ->
                    reduceOnlineUsers(state.currentScreen, msg.msg, state)
                is Msg.Call -> {
                    Napier.d("call ${msg.user}")
                    state to emptySet()
                }
                is Msg.ShowAlert, Msg.LoggedIn ->
                    throw IllegalStateException("$msg | $state")
            }
            ScreenState.Call -> when (msg) {
                else -> throw IllegalStateException("$msg | $state")
            }
            ScreenState.Login -> when (msg) {
                Msg.LoggedIn -> {
                    state.copy(
                        tabs = mapOf(
                            Tab.Main to listOf(OnlineUsers(OnlineUsersFeature.initialState()))
                        )
                    ) to OnlineUsersFeature.initialEffects()
                        .map { OnlineUsersEff(it) }
                        .toSet()
                }
                is Msg.OnlineUsersMsg, is Msg.Call, is Msg.ShowAlert ->
                    throw IllegalStateException("$msg | $state")
            }
        }
    }

    private fun reduceOnlineUsers(
        currentScreen: OnlineUsers,
        msg: OnlineUsersFeature.Msg,
        state: State
    ): ReducerResult {
        val (newScreenState, effs) = OnlineUsersFeature.reducer(msg, currentScreen.state)
        val newEffs = effs.mapTo(HashSet(), Eff::OnlineUsersEff)
        return state.changeCurrentScreen<OnlineUsers> { copy(state = newScreenState) } to newEffs
    }
}
