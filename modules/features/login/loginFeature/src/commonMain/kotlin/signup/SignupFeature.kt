package com.well.modules.features.login.loginFeature.signup

import com.well.modules.puerhBase.toSetOf

object SignupFeature {
    data class State(
        val processing: Boolean = false,
        val signupInfo: SignupInfo = SignupInfo("", "", "", ""),
    )

    sealed class Msg {
        class Create(val info: SignupInfo) : Msg()
        object SignIn : Msg()
    }

    sealed interface Eff {
        class Create(val info: SignupInfo) : Eff
        object SignIn : Eff
    }

    fun reducer(
        msg: Msg,
        state: State
    ): Pair<State, Set<Eff>> = when (msg) {
        is Msg.Create -> {
            state.copy(
                processing = true,
                signupInfo = msg.info
            ) toSetOf Eff.Create(msg.info)
        }
        Msg.SignIn -> {
            state toSetOf Eff.SignIn
        }
    }
}