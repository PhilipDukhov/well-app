package com.well.modules.features.more.moreFeature.subfeatures

import com.well.modules.features.more.moreFeature.MoreFeature
import com.well.modules.models.User
import com.well.modules.models.date.Date
import com.well.modules.puerhBase.toSetOf
import com.well.modules.puerhBase.withEmptySet

object ActivityHistoryFeature {
    data class State(
        val callHistoryItems: List<CallHistoryItem> = emptyList(),
        val searchHistoryItems: List<SearchHistoryItem> = emptyList(),
    ) {
        val title = MoreFeature.State.Item.ActivityHistory.title

        data class CallHistoryItem(
            val user: User,
            val date: Date,
            val isIncomingCall: Boolean,
            val isVideoCall: Boolean,
        )

        data class SearchHistoryItem(
            val text: String,
            val date: Date,
        )
    }

    sealed class Msg {
        class UpdateCallHistoryItems(val callHistoryItems: List<State.CallHistoryItem>) : Msg()
        class UpdateSearchHistoryItems(val searchHistoryItems: List<State.SearchHistoryItem>) : Msg()
        object Back : Msg()
    }

    sealed interface Eff {
        object Back : Eff
    }

    fun reducer(
        msg: Msg,
        state: State,
    ): Pair<State, Set<Eff>> = run state@{
        return@reducer state toSetOf (run eff@{
            when (msg) {
                Msg.Back -> {
                    return@eff Eff.Back
                }
                is Msg.UpdateCallHistoryItems -> {
                    return@state state.copy(callHistoryItems = msg.callHistoryItems)
                }
                is Msg.UpdateSearchHistoryItems -> {
                    return@state state.copy(searchHistoryItems = msg.searchHistoryItems)
                }
            }
        })
    }.withEmptySet()
}