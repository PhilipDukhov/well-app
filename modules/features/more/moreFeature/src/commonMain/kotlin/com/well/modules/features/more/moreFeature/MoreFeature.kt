package com.well.modules.features.more.moreFeature

import com.well.modules.features.more.moreFeature.subfeatures.AboutFeature
import com.well.modules.features.more.moreFeature.subfeatures.ActivityHistoryFeature
import com.well.modules.features.more.moreFeature.subfeatures.DonateFeature
import com.well.modules.features.more.moreFeature.subfeatures.FavoritesFeature
import com.well.modules.features.more.moreFeature.subfeatures.SupportFeature
import com.well.modules.features.more.moreFeature.subfeatures.WellAcademyFeature
import com.well.modules.puerhBase.toSetOf
import com.well.modules.utils.kotlinUtils.spacedUppercaseName

object MoreFeature {
    class State {
        val title = "More"
        val items = Item.values().toList()

        enum class Item {
            InviteColleague,
            Favorites,
            WellAcademy,
            ActivityHistory,
            Donate,
            TechnicalSupport,
            About,
            ;

            val title
                get() = when (this) {
                    WellAcademy -> "WELL Academy"
                    Donate -> "Sponsor & donate"
                    else -> spacedUppercaseName()
                }
        }
    }

    sealed class Msg {
        data class SelectItem(val item: State.Item) : Msg()
    }

    sealed interface Eff {
        data class Push(val screen: MoreScreenState) : Eff
        object InviteColleague : Eff
    }

    fun reducer(
        msg: Msg,
        state: State,
    ): Pair<State, Set<Eff>> = state toSetOf run eff@{
        when (msg) {
            is Msg.SelectItem -> {
                Eff.Push(
                    when (msg.item) {
                        State.Item.InviteColleague -> return@eff Eff.InviteColleague

                        State.Item.TechnicalSupport -> MoreScreenState.Support(SupportFeature.State)
                        State.Item.About -> MoreScreenState.About(AboutFeature.State())
                        State.Item.WellAcademy -> MoreScreenState.WellAcademy(WellAcademyFeature.State)
                        State.Item.Favorites -> MoreScreenState.Favorites(FavoritesFeature.State())
                        State.Item.ActivityHistory -> MoreScreenState.ActivityHistory(ActivityHistoryFeature.State())
                        State.Item.Donate -> MoreScreenState.Donate(DonateFeature.State)
                    }
                )
            }
        }
    }
}