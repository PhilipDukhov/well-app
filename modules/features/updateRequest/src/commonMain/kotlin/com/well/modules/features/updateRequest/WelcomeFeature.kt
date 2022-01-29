package com.well.modules.features.updateRequest

import com.well.modules.puerhBase.toSetOf

object UpdateRequestFeature {
    object State {
        const val text = "Update is required"
    }

    sealed class Msg {
        object Update : Msg()
    }

    sealed interface Eff {
        object Update : Eff
    }

    fun reducer(
        msg: Msg,
        state: State,
    ): Pair<State, Set<Eff>> = when (msg) {
        is Msg.Update -> {
            state toSetOf Eff.Update
        }
    }
}