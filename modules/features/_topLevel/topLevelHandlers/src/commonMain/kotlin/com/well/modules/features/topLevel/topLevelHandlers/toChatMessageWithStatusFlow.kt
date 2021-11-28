package com.well.modules.features.topLevel.topLevelHandlers

import com.well.modules.db.chatMessages.select
import com.well.modules.db.meetings.getByIdsFlow
import com.well.modules.db.mobile.DatabaseProvider
import com.well.modules.models.User
import com.well.modules.models.chat.ChatMessage
import com.well.modules.models.chat.ChatMessageContainer
import com.well.modules.models.chat.ChatMessageViewModel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

fun Flow<List<ChatMessage>>.toChatMessageContainerFlow(
    currentUid: User.Id,
    databaseProvider: DatabaseProvider,
) = flatMapLatest { chatList ->
    Napier.i("toChatMessageContainerFlow $chatList")
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
    val meetingsFlow = databaseProvider.meetingsQueries
        .getByIdsFlow(
            chatList.mapNotNull {
                (it.content as? ChatMessage.Content.Meeting)?.meetingId
            }
        )

    lastReadMessagesFlow
        .combine(meetingsFlow) { lastReadMessages, meetings ->
            val lastReadMessagesMap = lastReadMessages
                .groupBy { it.peerId to it.fromId }
                .mapValues { it.value.first() }
            val meetingsMap = meetings
                .groupBy { it.id }
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
                        content = ChatMessageViewModel.Content.create(
                            content = message.content,
                            getMeeting = meetingsMap::get,
                        ),
                    )
                )
            }
        }
        .map { messages -> messages.sortedByDescending { it.message.creation } }
}