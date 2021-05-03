package com.well.sharedMobile.puerh.login.signup

import com.well.modules.utils.toSetOf

object SignupFeature {
    data class State(
        val processing: Boolean = false,
        val signupInfo: SignupInfo = SignupInfo("", "", "", ""),
    )

    sealed class Msg {
        data class Create(val info: SignupInfo) : Msg()
        object SignIn : Msg()
    }

    sealed class Eff {
        data class Create(val info: SignupInfo) : Eff()
        object SignIn : Eff()
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