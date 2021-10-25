package com.well.modules.features.login.loginFeature

import com.well.modules.puerhBase.toSetOf
import com.well.modules.puerhBase.withEmptySet

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