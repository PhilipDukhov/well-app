package com.well.modules.features.chatList.chatListHandlers

import com.well.modules.features.chatList.chatListFeature.ChatListFeature
import com.well.modules.features.chatList.chatListFeature.ChatListFeature.Eff
import com.well.modules.features.chatList.chatListFeature.ChatListFeature.Msg
import com.well.modules.models.ChatMessageId
import com.well.modules.models.User
import com.well.modules.models.UserId
import com.well.modules.models.WebSocketMsg
import com.well.modules.models.chat.ChatMessage
import com.well.modules.models.chat.ChatMessageWithStatus
import com.well.modules.models.chat.LastReadMessage
import com.well.modules.puerhBase.EffectHandler
import com.well.modules.utils.flowUtils.combineToUnit
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class ChatListEffHandler(
    private val currentUid: UserId,
    private val services: Services,
    coroutineScope: CoroutineScope,
) : EffectHandler<Eff, Msg>(coroutineScope) {
    data class Services(
        val openUserChat: (id: UserId) -> Unit,
        val onConnectedFlow: Flow<Unit>,
        val lastListWithStatusFlow: Flow<List<ChatMessageWithStatus>>,
        val unreadCountsFlow: (List<ChatMessage>) -> Flow<Map<ChatMessageId, Long>>,
        val getUsersByIdsFlow: (List<UserId>) -> Flow<List<User>>,
        val lastPresentMessageIdFlow: Flow<ChatMessageId>,
        val lastReadPresenceFlow: Flow<List<LastReadMessage>>,
        val sendFrontWebSocketMsg: suspend (WebSocketMsg.Front) -> Unit,
    )

    private val chatListItemsFlow = services
        .lastListWithStatusFlow
        .flatMapLatest { chatMessagesWithStatus ->
            val unreadCountsFlow = services.unreadCountsFlow(chatMessagesWithStatus.map { it.message })
            services.getUsersByIdsFlow(chatMessagesWithStatus.map { it.message.secondId(currentUid) })
                .combine(unreadCountsFlow) { users, unreadCounts ->
                    chatMessagesWithStatus.mapNotNull { messageWithStatus ->
                        ChatListFeature.State.ListItem(
                            user = users.firstOrNull {
                                it.id == messageWithStatus.message.secondId(currentUid)
                            } ?: return@mapNotNull null,
                            lastMessage = messageWithStatus,
                            unreadCount = unreadCounts[messageWithStatus.message.id]?.toInt()
                                ?: 0,
                        )
                    }
                }
        }

    init {
        coroutineScope.launch {
            chatListItemsFlow
                .combineToUnit(services.onConnectedFlow)
                .collect { chatList ->
                    listener(Msg.UpdateItems(chatList))
                }
        }
        coroutineScope.launch {
            services.lastPresentMessageIdFlow
                .combineToUnit(services.onConnectedFlow)
                .collect { lastPresentMessageId ->
                    Napier.i("WebSocketMsg.Front.SetChatMessagePresence $lastPresentMessageId")
                    services.sendFrontWebSocketMsg(
                        WebSocketMsg.Front.SetChatMessagePresence(
                            messagePresenceId = lastPresentMessageId
                        )
                    )
                }
        }
        coroutineScope.launch {
            services.lastReadPresenceFlow
                .combineToUnit(services.onConnectedFlow)
                .collect { lastReadPresence ->
                    services.sendFrontWebSocketMsg(
                        WebSocketMsg.Front.UpdateChatReadStatePresence(
                            lastReadPresence
                        )
                    )
                }
//            networkManager.onConnectedFlow
//                .collect {
//                    val lastReadMessages = messagesDatabase.lastReadMessagesQueries
//                        .selectAll()
//                        .executeAsList()
//                        .map(LastReadMessages::toLastReadMessage)
//                    println("lastReadPresence $lastReadMessages")
//                    networkManager.send(
//                        WebSocketMsg.Front.UpdateChatReadStatePresence(
//                            lastReadMessages
//                        )
//                    )
//                }
        }
    }

    override suspend fun processEffect(eff: Eff) {
        when (eff) {
            is Eff.SelectChat -> {
                services.openUserChat(eff.uid)
            }
        }
    }
}