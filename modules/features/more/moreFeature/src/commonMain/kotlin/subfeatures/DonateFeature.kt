package com.well.modules.features.more.moreFeature.subfeatures

import com.well.modules.puerhBase.toSetOf
import com.well.modules.utils.viewUtils.GlobalStringsBase

object DonateFeature {
    object Strings: GlobalStringsBase() {
        const val title = "Donate"
        const val text = "Adipisicing reprehenderit qui commodo id quis id qui dolor nostrud. Nostrud cupidatat sint ipsum labore amet enim magna cillum labore ad excepteur ad cillum."
        const val howMuch = "How much would you like to donate?"
        const val isRecurring = "This is a recurring donation"
    }

    object State {
        val variants = listOf(10, 50, 100).map(State::Variant)

        data class Variant(val price: Int)
    }

    sealed class Msg {
        object Back : Msg()
        class Donate(val variant: State.Variant, val isRecurring: Boolean): Msg()
    }

    sealed interface Eff {
        object Back : Eff
        class Donate(val variant: State.Variant, val isRecurring: Boolean): Eff
    }

    fun reducer(
        msg: Msg,
        state: State,
    ): Pair<State, Set<Eff>> =
        state toSetOf when (msg) {
            Msg.Back -> Eff.Back
            is Msg.Donate -> Eff.Donate(msg.variant, msg.isRecurring)
        }
}