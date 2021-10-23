package com.well.modules.viewHelpers.chatMessageWithStatus

import com.well.modules.db.chatMessages.ChatMessagesDatabase
import com.well.modules.flowHelper.asSingleFlow
import com.well.modules.flowHelper.mapIterable
import com.well.modules.models.UserId
import com.well.modules.models.chat.ChatMessage
import io.github.aakira.napier.Napier
import com.well.modules.viewHelpers.chatMessageWithStatus.ChatMessageWithStatus.Status
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

private sealed class ChatMessageStatusContainer(open val message: ChatMessage) {
    data class Status(val status: ChatMessageWithStatus.Status, override val message: ChatMessage) :
        ChatMessageStatusContainer(message)

    data class TBD(override val message: ChatMessage) : ChatMessageStatusContainer(message)
}

fun Flow<List<ChatMessage>>.toChatMessageWithStatusFlow(
    currentUid: UserId,
    messagesDatabase: ChatMessagesDatabase,
) = flatMapLatest { chatList ->
    Napier.i("toChatMessageWithStatusFlow $chatList")
    val containers = chatList
        .map { message ->
            when {
                message.id < 0 -> {
                    ChatMessageStatusContainer.Status(Status.OutgoingSending, message)
                }
                else -> {
                    ChatMessageStatusContainer.TBD(message)
                }
            }
        }
    val tbdContainers = containers
        .mapNotNull { it as? ChatMessageStatusContainer.TBD }
    val statusContainers = containers.mapNotNull { it as? ChatMessageStatusContainer.Status }
    if (tbdContainers.isNotEmpty())
        messagesDatabase
            .lastReadMessagesQueries
            .select(
                fromAndPeerIds = tbdContainers
                    .groupBy {
                        it.message.peerId to it.message.fromId
                    }
                    .map { entries ->
                        entries.value.first().let {
                            "${it.message.fromId}|${it.message.peerId}"
                        }
                    }
            )
            .asFlow()
            .mapToList()
            .map { lastReadMessages ->
                val lastReadMessagesMap = lastReadMessages.groupBy {
                    it.peerId to it.fromId
                }.mapValues { it.value.first() }
                val lastReadContainers = tbdContainers
                    .map { container ->
                        val key = container.message.let { it.peerId to it.fromId }
                        val lastReadMessageId = lastReadMessagesMap[key]?.messageId
                        val read = container.message.id <= lastReadMessageId ?: -1
                        val status = if (container.message.peerId == currentUid) {
                            if (read) Status.IncomingRead
                            else Status.IncomingUnread
                        } else {
                            if (read) Status.OutgoingRead
                            else Status.OutgoingSent
                        }
                        ChatMessageStatusContainer.Status(
                            status = status,
                            message = container.message
                        )
                    }
                statusContainers + lastReadContainers
            }
    else {
        statusContainers.asSingleFlow()
    }
        .mapIterable {
            ChatMessageWithStatus(
                message = it.message,
                status = it.status,
            )
        }
        .map { messages -> messages.sortedByDescending { it.message.creation } }
}