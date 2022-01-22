package com.well.modules.features.more

import com.well.modules.features.more.about.AboutFeature
import com.well.modules.features.more.support.SupportFeature
import com.well.modules.features.more.wellAcademy.WellAcademyFeature
import com.well.modules.puerhBase.toSetOf

object MoreFeature {
    class State {
        val items = Item.values().toList()

        enum class Item {
            WellAcademy,
            Support,
            About,
            ;
        }
    }

    sealed class Msg {
        data class SelectItem(val item: State.Item) : Msg()
    }

    sealed interface Eff {
        data class Push(val screen: MoreScreenState) : Eff
    }

    fun reducer(
        msg: Msg,
        state: State,
    ): Pair<State, Set<Eff>> = state toSetOf when (msg) {
        is Msg.SelectItem -> {
            Eff.Push(
                when (msg.item) {
                    State.Item.Support -> MoreScreenState.Support(SupportFeature.State())
                    State.Item.About -> MoreScreenState.About(AboutFeature.State())
                    State.Item.WellAcademy -> MoreScreenState.WellAcademy(WellAcademyFeature.State())
                }
            )
        }
    }
}