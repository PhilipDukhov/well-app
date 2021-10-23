package com.well.modules.features.login.signup

import com.well.modules.utils.toSetOf
import com.well.modules.utils.withEmptySet

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

    sealed class Eff {
        data class Send(val info: String, val method: State.Method) : Eff()
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