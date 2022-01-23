package com.well.modules.features.more.support

import com.well.modules.features.more.MoreFeature
import com.well.modules.puerhBase.toSetOf

object SupportFeature {
    class State {
        val title = MoreFeature.State.Item.TechnicalSupport.title
        val text = "Please write us a message and we will get back to you shortly."
        val maxCharacters = 300
    }

    sealed class Msg {
        data class Send(val text: String) : Msg()
        object Back : Msg()
    }

    sealed interface Eff {
        data class Send(val text: String) : Eff
        object Back : Eff
    }

    fun reducer(
        msg: Msg,
        state: State,
    ): Pair<State, Set<Eff>> =
        state toSetOf when (msg) {
            Msg.Back -> Eff.Back
            is Msg.Send -> Eff.Send(msg.text)
        }
}