package com.well.modules.features.login.loginFeature.signup

import com.well.modules.puerhBase.toSetOf
import com.well.modules.puerhBase.withEmptySet

object LoginOtpFeature {
    data class State(
        val processing: Boolean = false,
        val selectedMethod: Method = Method.Email,
        val editedInfo: String = "",
        internal val signupInfo: SignupInfo? = null,
    ) {
        enum class Method {
            Email,
            Sms,
            ;
        }

        val currentInfo: String =
            if (signupInfo != null) {
                when (selectedMethod) {
                    Method.Email -> signupInfo.email
                    Method.Sms -> signupInfo.firstName
                }
            } else {
                editedInfo
            }
        val editable = signupInfo == null
    }

    sealed class Msg {
        data class Send(val info: String) : Msg()
        object SwitchMethod : Msg()
    }

    sealed interface Eff {
        data class Send(val info: String, val method: State.Method) : Eff
    }

    fun reducer(
        msg: Msg,
        state: State
    ): Pair<State, Set<Eff>> = when (msg) {
        is Msg.Send -> {
            state.copy(
                processing = true,
                editedInfo = msg.info
            ) toSetOf Eff.Send(msg.info, state.selectedMethod)
        }
        Msg.SwitchMethod -> {
            state.copy(
                selectedMethod = when (state.selectedMethod) {
                    State.Method.Email -> State.Method.Sms
                    State.Method.Sms -> State.Method.Email
                },
                editedInfo = "",
            ).withEmptySet()
        }
    }
}