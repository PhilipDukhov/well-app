package com.well.sharedMobile.puerh.login

import com.well.modules.utils.base.toSetOf
import com.well.modules.utils.base.withEmptySet

object LoginFeature {
    data class State(
        val processing: Boolean = false,
    )

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