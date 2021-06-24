package com.well.sharedMobile.puerh.chatList

import com.well.modules.db.chatMessages.ChatMessages
import com.well.modules.db.chatMessages.ChatMessagesDatabase
import com.well.modules.db.chatMessages.LastReadMessages
import com.well.modules.db.chatMessages.messagePresenceFlow
import com.well.modules.db.chatMessages.toChatMessage
import com.well.modules.db.chatMessages.toLastReadMessage
import com.well.modules.db.users.Users
import com.well.modules.db.users.UsersDatabase
import com.well.modules.db.users.toUser
import com.well.modules.flowHelper.asSingleFlow
import com.well.modules.flowHelper.flattenFlow
import com.well.modules.flowHelper.mapIterable
import com.well.modules.models.ChatMessageId
import com.well.modules.models.UserId
import com.well.modules.models.WebSocketMsg
import com.well.modules.utils.puerh.EffectHandler
import com.well.sharedMobile.networking.NetworkManager
import com.well.sharedMobile.networking.combineToNetworkConnectedState
import com.well.sharedMobile.puerh.chatList.ChatListFeature.Eff
import com.well.sharedMobile.puerh.chatList.ChatListFeature.Msg
import com.well.sharedMobile.puerh.πModels.chatMessageWithStatus.toChatMessageWithStatusFlow
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature.Eff as TopLevelEff
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature.Msg as TopLevelMsg
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ChatListEffHandler(
    private val currentUid: UserId,
    private val networkManager: NetworkManager,
    private val usersDatabase: UsersDatabase,
    private val messagesDatabase: ChatMessagesDatabase,
    coroutineScope: CoroutineScope,
) : EffectHandler<TopLevelEff, TopLevelMsg>(coroutineScope) {
    private val messagesFlow =
        messagesDatabase
            .chatMessagesQueries
            .lastList(currentUid)
            .asFlow()
            .mapToList()

    private val messagesWithStatusFlow = messagesFlow
        .mapIterable(ChatMessages::toChatMessage)
        .toChatMessageWithStatusFlow(currentUid = currentUid, messagesDatabase = messagesDatabase)

    private val chatListItemsFlow = messagesWithStatusFlow
        .flatMapLatest { chatMessagesWithStatus ->
            val unreadCountsFlow = chatMessagesWithStatus.map { message ->
                messagesDatabase.chatMessagesQueries
                    .unreadCount(fromId = message.message.secondId(currentUid), peerId = currentUid)
                    .asFlow()
                    .mapToOne()
                    .map { unreadCount ->
                        message.message.id to unreadCount
                    }
            }
                .flattenFlow()
                .map { it.toMap() }
            usersDatabase.usersQueries
                .getByIds(chatMessagesWithStatus.map { it.message.secondId(currentUid) })
                .asFlow()
                .mapToList()
                .combine(unreadCountsFlow) { dbUsers, unreadCounts ->
                    val users = dbUsers.map(Users::toUser)
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
    private val lastPresentMessageIdFlow: Flow<ChatMessageId> =
        messagesDatabase.chatMessagesQueries
            .messagePresenceFlow()
    private val lastReadPresenceFlow =
        messagesDatabase.lastReadMessagesQueries
            .selectAll()
            .asFlow()
            .mapToList()
            .mapIterable(LastReadMessages::toLastReadMessage)

    init {
        coroutineScope.launch {
            chatListItemsFlow
                .combineToNetworkConnectedState(networkManager)
                .collect { chatList ->
                    sendListenerMsg(Msg.UpdateItems(chatList))
                }
        }
        coroutineScope.launch {
            lastPresentMessageIdFlow
                .combineToNetworkConnectedState(networkManager)
                .collect { lastPresentMessageId ->
                    println("lastPresentMessageId $lastPresentMessageId")
                    networkManager.send(
                        WebSocketMsg.Front.SetChatMessagePresence(
                            messagePresenceId = lastPresentMessageId
                        )
                    )
                }
        }
        coroutineScope.launch {
            lastReadPresenceFlow
                .combineToNetworkConnectedState(networkManager)
                .collect { lastReadPresence ->
                    println("lastReadPresence $lastReadPresence")
                    networkManager.send(
                        WebSocketMsg.Front.UpdateChatReadStatePresence(
                            lastReadPresence
                        )
                    )
                }
        }
    }

    private fun sendListenerMsg(msg: Msg) {
        listener?.invoke(TopLevelMsg.ChatListMsg(msg))
    }

    override fun handleEffect(eff: TopLevelEff) {
        if (eff !is TopLevelEff.ChatListEff) return
        when (eff.eff) {
            is Eff.SelectChat -> {
                listener?.invoke(TopLevelMsg.OpenUserChat(eff.eff.uid))
            }
        }
    }
}