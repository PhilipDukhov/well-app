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
            "State($currentScreen at $selectedScreenPosition; tabs=$tabs)"

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
        object Back : Msg()
    }

    sealed class Eff {
        data class ShowAlert(val alert: Alert) : Eff()
        object SystemBack : Eff()
        object TestLogin : Eff()
        data class GotLogInToken(val token: String) : Eff()
        data class OnlineUsersEff(val eff: OnlineUsersFeature.Eff) : Eff()
        data class CallEff(val eff: CallFeature.Eff) : Eff()
        data class ImageSharingEff(val eff: ImageSharingFeature.Eff) : Eff()
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
                            return@reducer state.reduceCall(CallFeature.Msg.End)
                        }
                        is ScreenState.ImageSharing -> {
                            return@reducer state.reduceImageSharing(ImageSharingFeature.Msg.Close)
                        }
                    }
                }
                is Msg.LoggedIn -> {
                    return@state state.copyLogin()
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
                is Msg.OnlineUsersMsg -> {
                    return@reducer state.reduceOnlineUsers(msg.msg)
                }
                is Msg.CallMsg -> {
                    return@reducer state.reduceCall(msg.msg)
                }
                is Msg.StartImageSharing -> {
                    return@reducer state.reduceStartImageSharing(msg)
                }
                is Msg.ImageSharingMsg ->
                    return@reducer state.reduceImageSharing(
                        msg.msg,
                    )
                is Msg.StopImageSharing -> {
                    return@state state.copyPop(Tab.Overlay)
                }
                is Msg.EndCall -> {
                    return@state state.copyHideOverlay()
                }
            }
        })
    }.withEmptySet()

    private fun State.copyLogin(): State =
        copy(
            tabs = mapOf(
                Tab.Main to listOf(ScreenState.OnlineUsers(OnlineUsersFeature.initialState()))
            ),
            selectedScreenPosition = ScreenPosition(Tab.Main, 0),
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

    private fun State.reduceStartImageSharing(msg: Msg.StartImageSharing): ReducerResult {
        val (state, effs) = ImageSharingFeature.initialState(msg.role)
        return copyPush(
            Tab.Overlay,
            ScreenState.ImageSharing(state),
        ) to effs.mapTo(HashSet(), Eff::ImageSharingEff)
    }

    private fun State.reduceOnlineUsers(
        msg: OnlineUsersFeature.Msg,
    ): ReducerResult {
        val (screen, position) = tabs.screenAndPositionOfFirstOrNull<ScreenState.OnlineUsers>()
            ?: throw IllegalStateException("$msg | $this")
        val (newScreenState, effs) = OnlineUsersFeature.reducer(msg, screen.state)
        val newEffs = effs.mapTo(HashSet(), Eff::OnlineUsersEff)
        return changeScreen<ScreenState.OnlineUsers>(position) {
            copy(state = newScreenState)
        } to newEffs
    }

    private fun State.reduceCall(
        msg: CallFeature.Msg,
    ): ReducerResult {
        val (screen, position) = tabs.screenAndPositionOfFirstOrNull<ScreenState.Call>()
            ?: throw IllegalStateException("$msg | $this")
        val (newScreenState, effs) = CallFeature.reducer(msg, screen.state)
        val newEffs = effs.mapTo(HashSet(), Eff::CallEff)
        return changeScreen<ScreenState.Call>(position) {
            copy(state = newScreenState)
        } to newEffs
    }

    private fun State.reduceImageSharing(
        msg: ImageSharingFeature.Msg,
    ): ReducerResult {
        val (screen, position) = tabs.screenAndPositionOfFirstOrNull<ScreenState.ImageSharing>()
            ?: return this.withEmptySet()
        val (newScreenState, effs) = ImageSharingFeature.reducer(msg, screen.state)
        val newEffs = effs.mapTo(HashSet(), Eff::ImageSharingEff)
        return changeScreen<ScreenState.ImageSharing>(position) {
            copy(state = newScreenState)
        } to newEffs
    }
}
