package com.well.modules.features.chatList.chatListFeature

import com.well.modules.models.User
import com.well.modules.models.chat.ChatMessageViewModel
import com.well.modules.puerhBase.toSetOf
import com.well.modules.puerhBase.withEmptySet

object ChatListFeature {
    data class State(
        internal val allListItems: List<ListItem> = listOf(),
        val filter: String? = null,
    ) {
        data class ListItem(
            val user: User,
            val lastMessage: ChatMessageViewModel,
            val unreadCount: Int,
        )

        val unreadCount = allListItems.sumOf { it.unreadCount }
        val listItems = if (filter == null) allListItems else
            allListItems.filter {
                it.user.fullName.contains(filter, ignoreCase = true)
            }
    }

    sealed class Msg {
        class SelectChat(val userId: User.Id) : Msg()
        class UpdateItems(val listItems: List<State.ListItem>) : Msg()
    }

    sealed interface Eff {
        class SelectChat(val uid: User.Id) : Eff
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