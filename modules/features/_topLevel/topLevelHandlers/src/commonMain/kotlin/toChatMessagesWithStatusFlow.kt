package com.well.modules.features.topLevel.topLevelHandlers

import com.well.modules.db.chatMessages.select
import com.well.modules.db.mobile.DatabaseProvider
import com.well.modules.models.User
import com.well.modules.models.chat.ChatMessage
import com.well.modules.models.chat.ChatMessageContainer
import com.well.modules.models.chat.ChatMessageViewModel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

fun Flow<List<ChatMessage>>.toChatMessagesContainerFlow(
    currentUid: User.Id,
    databaseProvider: DatabaseProvider,
): Flow<List<ChatMessageContainer>> = flatMapLatest {
    it.toChatMessagesContainerFlow(currentUid, databaseProvider)
}

fun List<ChatMessage>.toChatMessagesContainerFlow(
    currentUid: User.Id,
    databaseProvider: DatabaseProvider,
): Flow<List<ChatMessageContainer>> {
    val chatList = this
    val lastReadMessagesFlow = databaseProvider.messagesDatabase
        .lastReadMessagesQueries
        .select(
            fromAndPeerIds = chatList
                .filter { !it.id.isTmp }
                .groupBy {
                    it.peerId to it.fromId
                }
                .map { entries ->
                    entries.value.first().let {
                        it.fromId to it.peerId
                    }
                }
        )
    return lastReadMessagesFlow
        .map { lastReadMessages ->
            val lastReadMessagesMap = lastReadMessages
                .groupBy { it.peerId to it.fromId }
                .mapValues { it.value.first() }

            chatList.map { message ->
                val status = if (message.id.isTmp) {
                    ChatMessageViewModel.Status.OutgoingSending
                } else {
                    val key = message.let { it.peerId to it.fromId }
                    val lastReadMessageId = lastReadMessagesMap[key]?.messageId
                    val read = message.id <= (lastReadMessageId ?: ChatMessage.Id(-1))
                    if (message.peerId == currentUid) {
                        if (read) ChatMessageViewModel.Status.IncomingRead
                        else ChatMessageViewModel.Status.IncomingUnread
                    } else {
                        if (read) ChatMessageViewModel.Status.OutgoingRead
                        else ChatMessageViewModel.Status.OutgoingSent
                    }
                }
                ChatMessageContainer(
                    message = message,
                    viewModel = ChatMessageViewModel(
                        id = message.id,
                        creation = message.creation,
                        status = status,
                        content = message.content,
                    )
                )
            }
        }
        .map { messages -> messages.sortedByDescending { it.message.creation } }
}