package com.well.modules.features.more

import com.well.modules.utils.toSetOf
import com.well.modules.features.more.about.AboutFeature
import com.well.modules.features.more.support.SupportFeature

object MoreFeature {
    class State {
        val items = Item.values().toList()

        enum class Item {
            Support,
            About,
            ;
        }
    }

    sealed class Msg {
        data class SelectItem(val item: State.Item) : Msg()
    }

    sealed class Eff {
        data class Push(val screen: MoreScreenState) : Eff()
    }

    @Suppress("UNREACHABLE_CODE")
    fun reducer(
        msg: Msg,
        state: State
    ): Pair<State, Set<Eff>> = state toSetOf when (msg) {
        is Msg.SelectItem -> {
            Eff.Push(
                when (msg.item) {
                    State.Item.Support -> MoreScreenState.Support(SupportFeature.State())
                    State.Item.About -> MoreScreenState.About(AboutFeature.State())
                }
            )
        }
    }
}