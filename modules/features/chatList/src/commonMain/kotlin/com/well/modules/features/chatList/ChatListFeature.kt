package com.well.modules.features.chatList

import com.well.modules.models.User
import com.well.modules.models.UserId
import com.well.modules.utils.toSetOf
import com.well.modules.utils.withEmptySet
import com.well.modules.viewHelpers.chatMessageWithStatus.ChatMessageWithStatus

object ChatListFeature {
    data class State(
        internal val allListItems: List<ListItem> = listOf(),
        val filter: String? = null,
    ) {
        data class ListItem(
            val user: User,
            val lastMessage: ChatMessageWithStatus,
            val unreadCount: Int,
        )

        val unreadCount = allListItems.sumOf { it.unreadCount }
        val listItems = if (filter == null) allListItems else
            allListItems.filter {
                it.user.fullName.contains(filter, ignoreCase = true)
            }
    }

    sealed class Msg {
        data class SelectChat(val userId: UserId) : Msg()
        data class UpdateItems(val listItems: List<State.ListItem>) : Msg()
    }

    sealed class Eff {
        data class SelectChat(val uid: UserId) : Eff()
    }

    fun reducer(
        msg: Msg,
        state: State,
    ): Pair<State, Set<Eff>> = run state@{
        return@reducer state toSetOf (run eff@{
            when (msg) {
                is Msg.SelectChat -> {
                    return@eff Eff.SelectChat(msg.userId)
                }
                is Msg.UpdateItems -> {
                    return@state state.copy(allListItems = msg.listItems)
                }
            }
        })
    }.withEmptySet()
}