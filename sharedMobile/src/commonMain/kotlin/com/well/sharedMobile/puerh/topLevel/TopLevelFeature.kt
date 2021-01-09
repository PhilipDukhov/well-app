package com.well.sharedMobile.puerh.topLevel

import com.well.serverModels.User
import com.well.serverModels.WebSocketMessage
import com.well.sharedMobile.puerh.call.CallFeature
import com.well.sharedMobile.puerh.call.imageSharing.ImageSharingFeature
import com.well.sharedMobile.puerh.login.LoginFeature
import com.well.sharedMobile.puerh.onlineUsers.OnlineUsersFeature
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature.State.*
import com.well.utils.map
import com.well.utils.toSetOf
import com.well.utils.withEmptySet

object TopLevelFeature {
    fun initialState(
    ): State = State(
        mapOf(Tab.Main to listOf(ScreenState.Login(LoginFeature.State()))),
        ScreenPosition(Tab.Main, 0),
    )

    fun initialEffects(): Set<Eff> = setOf(
        Eff.TestLogin
    )

    data class State(
        val tabs: Map<Tab, List<ScreenState>>,
        val selectedScreenPosition: ScreenPosition,
    ) {
        val currentScreen = tabs.getValue(selectedScreenPosition.tab)[selectedScreenPosition.index]
        override fun toString() =
            "State(it at $selectedScreenPosition; tabs=$tabs)"

        fun <T : ScreenState> changeCurrentScreen(block: T.() -> T): State =
            changeScreen(selectedScreenPosition, block)

        fun <T : ScreenState> changeScreen(
            screenPosition: ScreenPosition,
            block: T.() -> T
        ): State =
            copy(tabs = tabs.copy(screenPosition, block))

        data class ScreenPosition(
            val tab: Tab,
            val index: Int
        )

        enum class Tab {
            Main,
            Overlay,
        }

        sealed class ScreenState {
            data class Login(val state: LoginFeature.State) : ScreenState()

            data class OnlineUsers(val state: OnlineUsersFeature.State) : ScreenState()

            data class Call(val state: CallFeature.State) : ScreenState()
            data class ImageSharing(val state: ImageSharingFeature.State) : ScreenState()
        }
    }

    sealed class Msg {
        data class OnlineUsersMsg(val msg: OnlineUsersFeature.Msg) : Msg()
        data class CallMsg(val msg: CallFeature.Msg) : Msg()
        data class ImageSharingMsg(val msg: ImageSharingFeature.Msg) : Msg()

        data class StartCall(val user: User) : Msg()
        data class IncomingCall(val incomingCall: WebSocketMessage.IncomingCall) : Msg()
        object EndCall : Msg()

        data class StartImageSharing(val role: ImageSharingFeature.State.Role) : Msg()
        object StopImageSharing : Msg()

        data class ShowAlert(val alert: Alert) : Msg()
        object LoggedIn : Msg()
    }

    sealed class Eff {
        data class ShowAlert(val alert: Alert) : Eff()
        object TestLogin : Eff()
        data class GotLogInToken(val token: String) : Eff()
        data class OnlineUsersEff(val eff: OnlineUsersFeature.Eff) : Eff()
        data class CallEff(val eff: CallFeature.Eff) : Eff()
        data class ImageSharingEff(val eff: ImageSharingFeature.Eff) : Eff()
    }

    fun reducer(
        msg: Msg,
        state: State
    ): ReducerResult {
        println("Top level reducer: $msg")
        return when (msg) {
            is Msg.ShowAlert -> state toSetOf Eff.ShowAlert(msg.alert)
            is Msg.IncomingCall -> {
                state.copyShowCall(
                    CallFeature.incomingInitialState(msg.incomingCall)
                )
                    .withEmptySet()
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
            is Msg.EndCall -> {
                state.copyHideOverlay()
                    .withEmptySet()
            }
            is Msg.StartImageSharing -> {
                state.reduceStartImageSharing(msg)
            }
            is Msg.StopImageSharing -> {
                state.copy(tabs = state.tabs.pop(Tab.Overlay))
                    .withEmptySet()
            }
            else -> when (state.currentScreen) {
                is ScreenState.Login -> when (msg) {
                    Msg.LoggedIn -> {
                        state.copyLogin()
                            .withEmptySet()
                    }
                    else -> throw IllegalStateException("$msg $state")
                }
                is ScreenState.OnlineUsers -> when (msg) {
                    is Msg.StartCall ->
                        CallFeature.callInitiateStateAndEffects(msg.user)
                            .map(
                                { state.copyShowCall(it) },
                                {
                                    it.map(Eff::CallEff)
                                        .toSet()
                                },
                            )
                    else -> throw IllegalStateException("$msg $state")
                }
                is ScreenState.Call -> when (msg) {
                    is Msg.CallMsg -> reduceCall(state.currentScreen, msg.msg, state)
                    else -> throw IllegalStateException("$msg $state")
                }
                is ScreenState.ImageSharing -> when (msg) {
                    is Msg.ImageSharingMsg -> reduceImageSharing(
                        state.currentScreen,
                        msg.msg,
                        state
                    )
                    else -> throw IllegalStateException("$msg $state")
                }
            }
        }
    }

    private fun State.copyLogin(): State =
        copy(
            tabs = mapOf(
                Tab.Main to listOf(ScreenState.OnlineUsers(OnlineUsersFeature.initialState()))
            ),
            selectedScreenPosition = ScreenPosition(Tab.Main, 0),
        )

    private fun State.copyShowCall(callState: CallFeature.State): State =
        copy(
            tabs = tabs.push(Tab.Overlay, ScreenState.Call(callState)),
            selectedScreenPosition = ScreenPosition(Tab.Overlay, 0),
        )

    private fun State.copyHideOverlay() =
        copy(
            tabs = tabs.remove(Tab.Overlay),
            selectedScreenPosition = ScreenPosition(
                Tab.Main,
                (tabs[Tab.Main]
                    ?: throw IllegalStateException("copyHideOverlay: main tab is unexpectedly empty"))
                    .lastIndex
            ),
        )

    // Screen reducers

    private fun State.reduceStartImageSharing(msg: Msg.StartImageSharing): ReducerResult {
        val (state, effs) = ImageSharingFeature.initialState(msg.role)
        return copy(
            tabs = tabs.push(Tab.Overlay, ScreenState.ImageSharing(state)),
            selectedScreenPosition = ScreenPosition(Tab.Overlay, 0),
        ) to effs.mapTo(HashSet(), Eff::ImageSharingEff)
    }

    private fun reduceOnlineUsers(
        screenAndPosition: Pair<ScreenState.OnlineUsers, ScreenPosition>,
        msg: OnlineUsersFeature.Msg,
        state: State
    ): ReducerResult {
        val (newScreenState, effs) = OnlineUsersFeature.reducer(msg, screenAndPosition.first.state)
        val newEffs = effs.mapTo(HashSet(), Eff::OnlineUsersEff)
        return state.changeScreen<ScreenState.OnlineUsers>(screenAndPosition.second) {
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

    private fun reduceImageSharing(
        currentScreen: ScreenState.ImageSharing,
        msg: ImageSharingFeature.Msg,
        state: State
    ): ReducerResult {
        val (newScreenState, effs) = ImageSharingFeature.reducer(msg, currentScreen.state)
        val newEffs = effs.mapTo(HashSet(), Eff::ImageSharingEff)
        return state.changeCurrentScreen<ScreenState.ImageSharing> { copy(state = newScreenState) } to newEffs
    }
}
