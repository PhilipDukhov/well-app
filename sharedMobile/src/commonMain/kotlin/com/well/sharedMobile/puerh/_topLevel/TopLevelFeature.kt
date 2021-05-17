package com.well.sharedMobile.puerh._topLevel

import com.well.modules.annotations.ScreenStates
import com.well.modules.models.User
import com.well.modules.models.WebSocketMsg
import com.well.sharedMobile.puerh.call.CallFeature
import com.well.sharedMobile.puerh.login.LoginFeature
import com.well.sharedMobile.puerh.experts.ExpertsFeature
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature.State.*
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature
import com.well.modules.utils.map
import com.well.modules.utils.toSetOf
import com.well.modules.utils.withEmptySet
import com.well.sharedMobile.puerh.welcome.WelcomeFeature

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
    ]
)
object TopLevelFeature {
    fun initialState(
    ): State = State(
        tabs = mapOf(Tab.Main to listOf(ScreenState.Launch)),
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
    }

    sealed class Msg {
        data class WelcomeMsg(val msg: WelcomeFeature.Msg) : Msg()
        data class MyProfileMsg(val msg: MyProfileFeature.Msg) : Msg()
        data class LoginMsg(val msg: LoginFeature.Msg) : Msg()
        data class ExpertsMsg(val msg: ExpertsFeature.Msg) : Msg()
        data class CallMsg(val msg: CallFeature.Msg) : Msg()

        data class StartCall(val user: User) : Msg()
        data class IncomingCall(val incomingCall: WebSocketMsg.IncomingCall) : Msg()
        object EndCall : Msg()

        object StopImageSharing : Msg()

        data class ShowAlert(val alert: Alert) : Msg()
        data class OpenUserProfile(
            val user: User,
            val isCurrent: Boolean
        ) : Msg()

        object LoggedIn : Msg()
        object OpenLoginScreen : Msg()
        object OpenWelcomeScreen : Msg()
        object Back : Msg()
        object Pop : Msg()
    }

    sealed class Eff {
        data class ShowAlert(val alert: Alert) : Eff()
        object SystemBack : Eff()
        object Initial : Eff()
        data class MyProfileEff(val eff: MyProfileFeature.Eff) : Eff()
        data class WelcomeEff(val eff: WelcomeFeature.Eff) : Eff()
        data class LoginEff(val eff: LoginFeature.Eff) : Eff()
        data class ExpertsEff(val eff: ExpertsFeature.Eff) : Eff()
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
                    return@reducer reduceBackMsg(state)
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
                            Tab.Main to listOf(ScreenState.Experts(ExpertsFeature.initialState()))
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
                is Msg.WelcomeMsg,
                is Msg.LoginMsg,
                is Msg.MyProfileMsg,
                is Msg.ExpertsMsg,
                is Msg.CallMsg,
                -> {
                    return@reducer reduceScreenMsg(msg, state)
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
                Msg.OpenWelcomeScreen -> {
                    return@state state.copyReplace(screen = ScreenState.Welcome(WelcomeFeature.State()))
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
}