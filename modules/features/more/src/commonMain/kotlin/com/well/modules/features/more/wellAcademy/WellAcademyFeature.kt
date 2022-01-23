package com.well.modules.features.more.wellAcademy

import com.well.modules.features.more.MoreFeature
import com.well.modules.puerhBase.toSetOf

object WellAcademyFeature {
    class State {
        val title = MoreFeature.State.Item.WellAcademy.title
        val text = "The WELL app provides urologists globally with an inexpensive and easily accessible means of performing mentored urological procedures in any environment. Coming soon"
        val items = Items.values().toList()

        enum class Items(val title: String) {
            Webinars("Webinars"),
            SemiLiveSurgeries("Semi-Live Surgeries"),
            Podcasts("Podcasts"),
            Blog("Blog"),
            JournalClub("Journal Club"),
            FundamentalsInEndourology("Fundamentals in Endourology"),
        }
    }

    sealed class Msg {
        object Back : Msg()
    }

    sealed interface Eff {
        object Back : Eff
    }

    fun reducer(
        msg: Msg,
        state: State,
    ): Pair<State, Set<Eff>> =
        state toSetOf when (msg) {
            is Msg.Back -> Eff.Back
        }
}