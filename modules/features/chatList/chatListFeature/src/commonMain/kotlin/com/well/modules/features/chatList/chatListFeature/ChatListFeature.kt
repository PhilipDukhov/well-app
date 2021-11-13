package com.well.modules.features.chatList.chatListFeature

import com.well.modules.models.User
import com.well.modules.puerhBase.toSetOf
import com.well.modules.puerhBase.withEmptySet
import com.well.modules.models.chat.ChatMessageWithStatus

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
        data class SelectChat(val userId: User.Id) : Msg()
        data class UpdateItems(val listItems: List<State.ListItem>) : Msg()
    }

    sealed interface Eff {
        data class SelectChat(val uid: User.Id) : Eff
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