package com.well.sharedMobile.puerh.login

import com.well.modules.utils.toSetOf
import com.well.modules.utils.withEmptySet

object LoginFeature {
    data class State(
        val processing: Boolean = false,
    ) {
        data class State(
            var firstName: String,
            var lastName: String,
            var email: String,
            var phoneNumber: String,
        )
    }

    sealed class Msg {
        data class OnSocialNetworkSelected(val socialNetwork: SocialNetwork) : Msg()
        object LoginAttemptFinished : Msg()
    }

    sealed class Eff {
        data class Login(val socialNetwork: SocialNetwork) : Eff()
    }

    fun reducer(
        msg: Msg,
        state: State
    ): Pair<State, Set<Eff>> = when (msg) {
        is Msg.OnSocialNetworkSelected -> {
            state.copy(processing = true) toSetOf Eff.Login(msg.socialNetwork)
        }
        Msg.LoginAttemptFinished -> {
            state.copy(processing = false).withEmptySet()
        }
    }
}