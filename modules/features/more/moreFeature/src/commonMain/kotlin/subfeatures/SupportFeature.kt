package com.well.modules.features.more.moreFeature.subfeatures

import com.well.modules.features.more.moreFeature.MoreFeature
import com.well.modules.puerhBase.toSetOf

object SupportFeature {
    data class State(val processing: Boolean = false) {
        val title = MoreFeature.State.Item.TechnicalSupport.title
        val text = "Please write us a message and we will get back to you shortly."
        val includeLogs = "Include logs"
        val maxCharacters = 300
    }

    sealed class Msg {
        class Send(val text: String, val includeLogs: Boolean) : Msg()
        object Back : Msg()
    }

    sealed interface Eff {
        class Send(val text: String, val includeLogs: Boolean) : Eff
        object Back : Eff
    }

    fun reducer(
        msg: Msg,
        state: State,
    ): Pair<State, Set<Eff>> = state toSetOf (run eff@{
        when (msg) {
            Msg.Back -> return@eff Eff.Back
            is Msg.Send -> {
                return@reducer state.copy(
                    processing = true
                ) toSetOf Eff.Send(msg.text, msg.includeLogs)
            }
        }
    })
}