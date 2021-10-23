package com.well.modules.features.more.support

import com.well.modules.utils.toSetOf

object SupportFeature {
    class State {
        val text = "Please write us a message and we will get back to you shortly."
        val maxCharacters = 300

        companion object {
            const val title = "Technical support"
        }
    }

    sealed class Msg {
        data class Send(val text: String) : Msg()
        object Back : Msg()
    }

    sealed class Eff {
        data class Send(val text: String) : Eff()
        object Back : Eff()
    }

    fun reducer(
        msg: Msg,
        state: State
    ): Pair<State, Set<Eff>> =
        state toSetOf when (msg) {
            Msg.Back -> Eff.Back
            is Msg.Send -> Eff.Send(msg.text)
        }
}