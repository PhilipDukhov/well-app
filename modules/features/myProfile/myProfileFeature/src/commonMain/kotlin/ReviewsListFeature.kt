package com.well.modules.features.myProfile.myProfileFeature

import com.well.modules.models.Availability
import com.well.modules.models.ExistingReview
import com.well.modules.models.Review
import com.well.modules.puerhBase.toSetOf
import com.well.modules.puerhBase.withEmptySet
import com.well.modules.utils.viewUtils.CalendarInfoFeature
import com.well.modules.utils.viewUtils.GlobalStringsBase

object ReviewsListFeature {
    object Strings : GlobalStringsBase() {
        const val title = "Reviews"
    }

    data class State(
        val currentUserReview: Review?,
        val reviews: List<ExistingReview> = listOf(),
    )

    sealed class Msg {
        object LeaveReview: Msg()
        class UpdateReviews(val reviews: List<ExistingReview>): Msg()
    }

    sealed interface Eff {
        object LeaveReview: Eff
    }

    fun reducer(
        msg: Msg,
        state: State,
    ): Pair<State, Set<Eff>> = run state@{
        return@reducer state toSetOf (run eff@{
            when (msg) {
                is Msg.LeaveReview -> {
                    return@eff Eff.LeaveReview
                }
                is Msg.UpdateReviews -> {
                    return@state state.copy(reviews = msg.reviews)
                }
            }
        })
    }.withEmptySet()
}