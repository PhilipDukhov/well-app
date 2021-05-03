package com.well.sharedMobile.puerh.welcome

import com.well.modules.utils.toSetOf

object WelcomeFeature {
    class State {
        val title = "Welcome to WELL app"
        val descriptions = listOf(
            "Our app provides urologists globally of performing mentored urological",
            "Our app provides urologists globally of performing mentored urological",
            "Our app provides urologists globally of performing mentored urological",
            "Our app provides urologists globally of performing mentored urological",
        )
    }

    sealed class Msg {
        object Continue : Msg()
    }

    sealed class Eff {
        object Continue : Eff()
    }

    fun reducer(
        msg: Msg,
        state: State
    ): Pair<State, Set<Eff>> = when (msg) {
        is Msg.Continue -> {
            state toSetOf Eff.Continue
        }
    }
}