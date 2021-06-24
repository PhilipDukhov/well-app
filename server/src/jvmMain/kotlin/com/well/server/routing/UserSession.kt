package com.well.server.routing

import com.well.modules.db.helper.adaptedIntersectionRegex
import com.well.modules.db.helper.adaptedOneOfRegex
import com.well.modules.db.server.ChatMessages
import com.well.modules.db.server.LastReadMessages
import com.well.modules.db.server.insertChatMessage
import com.well.modules.db.server.toLastReadMessage
import com.well.modules.db.server.toChatMessage
import com.well.modules.flowHelper.MutableSetFlow
import com.well.modules.flowHelper.asSingleFlow
import com.well.modules.flowHelper.filterIterable
import com.well.modules.flowHelper.filterNotEmpty
import com.well.modules.flowHelper.flatMapLatest
import com.well.modules.flowHelper.mapIterable
import com.well.modules.models.chat.ChatMessage
import com.well.modules.models.ChatMessageId
import com.well.modules.models.UserId
import com.well.modules.models.UserPresenceInfo
import com.well.modules.models.UsersFilter
import com.well.modules.models.WebSocketMsg
import com.well.modules.models.chat.LastReadMessage
import com.well.server.utils.CallInfo
import com.well.server.utils.Dependencies
import com.well.server.utils.send
import com.well.server.utils.toLong
import com.well.server.utils.toUser
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.joda.time.Weeks
import java.util.Date

class UserSession(
    private val currentUid: UserId,
    private val webSocketSession: WebSocketSession,
    private val dependencies: Dependencies,
) : WebSocketSession by webSocketSession {
    private class CreatedMessageInfo(
        val tmpId: ChatMessageId,
        val id: ChatMessageId,
    )

    private val expertsFilterFlow = MutableStateFlow<UsersFilter?>(null)
    private val usersPresenceInfoFlow = MutableStateFlow<List<UserPresenceInfo>?>(null)
    private val messagesPresenceInfoFlow = MutableStateFlow<ChatMessageId?>(null)
    private val chatReadStatePresenceFlow = MutableStateFlow<List<LastReadMessage>>(emptyList())
    private val userCreatedChatMessageInfosFlow = MutableSetFlow<CreatedMessageInfo>()

    private val expertsFlow: Flow<List<UserId>> = expertsFilterFlow
        .filterNotNull()
        .flatMapLatest { filter ->
            val maxLastOnlineDistance =
                if (!filter.activity.contains(UsersFilter.Activity.LastWeek)) 0 else
                    Weeks.ONE.toStandardSeconds().seconds.toLong()
            val specificIdsRegexpFlow =
                if (!filter.activity.contains(UsersFilter.Activity.Now)) "".asSingleFlow() else
                    dependencies
                        .connectedUserSessionsFlow
                        .map { connectedUserSessions ->
                            connectedUserSessions
                                .map { it.key.toString() }
                                .adaptedOneOfRegex()
                        }

            specificIdsRegexpFlow.flatMapLatest { specificIdsRegexp ->
                dependencies.database.usersQueries
                    .filter(
                        nameFilter = filter.searchString,
                        favorites = filter.favorite.toLong(),
                        uid = currentUid,
                        maxLastOnlineDistance = maxLastOnlineDistance,
                        specificIdsRegexp = specificIdsRegexp,
                        skillsRegexp = filter.skills.adaptedIntersectionRegex(),
                        academicRankRegexp = filter.academicRanks.adaptedIntersectionRegex(),
                        languagesRegexp = filter.languages.adaptedIntersectionRegex(),
                        countryCode = filter.countryCode ?: "",
                        withReviews = filter.withReviews.toLong(),
                        rating = filter.rating.toDoubleOrNull(),
                    )
                    .asFlow()
                    .mapToList()
            }
        }
    private val neededUsersFlow = expertsFlow
        .combine(
            dependencies.database.chatMessagesQueries
                .peerIdsList(id = currentUid)
                .asFlow()
                .mapToList()
        ) { users, peerIdsList ->
            users.toMutableSet().apply {
                add(currentUid)
                addAll(peerIdsList)
            }.toSet()
        }
    private val notUpToDateUsersFlow =
        usersPresenceInfoFlow
            .filterNotNull()
            .combine(neededUsersFlow) { usersPresenceInfo, neededUsers ->
                dependencies.database
                    .usersQueries
                    .getByIds(neededUsers)
                    .asFlow()
                    .mapToList()
                    .map { users ->
                        users.filter { user ->
                            usersPresenceInfo
                                .firstOrNull { it.id == user.id }
                                ?.lastEdited?.let { presenceLastEdited ->
                                    user.lastEdited > presenceLastEdited
                                } ?: true
                        }.map { it.toUser(currentUid, dependencies.database) }
                    }
            }
            .flatMapLatest()
            .filter { it.isNotEmpty() }
    private val newMessagesFlow =
        messagesPresenceInfoFlow
            .filterNotNull()
            .combine(userCreatedChatMessageInfosFlow) { messagesPresenceInfo, userCreatedChatMessageInfos ->
                dependencies.database.chatMessagesQueries
                    .getAllForUser(
                        id = currentUid,
                        lastPresentedId = messagesPresenceInfo,
                    )
                    .asFlow()
                    .mapToList()
                    .mapIterable { message ->
                        WebSocketMsg.Back.UpdateMessages.UpdateMessageInfo(
                            message.toChatMessage(),
                            tmpId = userCreatedChatMessageInfos.firstOrNull { it.id == message.id }?.tmpId,
                        )
                    }
            }
            .flatMapLatest()
    private val newReadStatesFlow =
        chatReadStatePresenceFlow
            .filterNotEmpty()
            .flatMapLatest { chatReadStatePresence ->
                dependencies.database.lastReadMessagesQueries
                    .select(
                        fromAndPeerIds = chatReadStatePresence.map {
                            "${it.fromId}|$currentUid"
                        }
                    )
                    .asFlow()
                    .mapToList()
                    .filterIterable { databaseLRM ->
                        chatReadStatePresence.first { presenceLRM ->
                            databaseLRM.fromId == presenceLRM.fromId
                        }.messageId > databaseLRM.messageId
                    }
                    .mapIterable(LastReadMessages::toLastReadMessage)
            }
            .filterNotEmpty()

    init {
        listOf(
            expertsFlow.map(WebSocketMsg.Back::ListFilteredExperts),
            notUpToDateUsersFlow.map(WebSocketMsg.Back::UpdateUsers),
            newMessagesFlow.map(WebSocketMsg.Back::UpdateMessages),
            newReadStatesFlow.map(WebSocketMsg.Back::UpdateCharReadPresence),
        ).forEach {
            launch { it.collect(::send) }
        }
    }

    suspend fun handleCallMsg(msg: WebSocketMsg.Call) {
        when (msg) {
            is WebSocketMsg.Call.Answer,
            is WebSocketMsg.Call.Candidate,
            is WebSocketMsg.Call.Offer,
            -> {
                dependencies.callPartnerId(currentUid)!!
                    .run {
                        dependencies.connectedUserSessionsFlow[value]!!
                            .send(msg)
                    }
            }
            is WebSocketMsg.Call.EndCall -> {
                dependencies.endCall(
                    currentUid,
                    msg.reason
                )
            }
        }
    }

    suspend fun handleFrontMsg(msg: WebSocketMsg.Front) {
        println("handleFrontMsg $currentUid $msg")
        when (msg) {
            is WebSocketMsg.Front.SetExpertsFilter -> {
                expertsFilterFlow.emit(msg.filter)
            }
            is WebSocketMsg.Front.SetUsersPresence -> {
                usersPresenceInfoFlow.emit(msg.usersPresence)
            }
            is WebSocketMsg.Front.InitiateCall -> {
                initiateCall(msg)
            }
            is WebSocketMsg.Front.CreateChatMessage -> {
                insertChatMessage(msg.message)
            }
            is WebSocketMsg.Front.SetChatMessagePresence -> {
                messagesPresenceInfoFlow.emit(msg.messagePresenceId)
            }
            is WebSocketMsg.Front.ChatMessageRead -> {
                chatMessageRead(msg.messageId)
            }
            is WebSocketMsg.Front.UpdateChatReadStatePresence -> {
                chatReadStatePresenceFlow.emit(msg.lastReadMessages)
            }
        }
    }

    private suspend fun initiateCall(msg: WebSocketMsg.Front.InitiateCall) {
        val session = dependencies.connectedUserSessionsFlow[msg.uid]
        if (session == null) {
            send(WebSocketMsg.Call.EndCall(WebSocketMsg.Call.EndCall.Reason.Offline))
            return
        }
        if (dependencies.callInfos.any { it.uids.contains(currentUid) }) {
            send(WebSocketMsg.Call.EndCall(WebSocketMsg.Call.EndCall.Reason.Busy))
            return
        }
        session.send(
            WebSocketMsg.Back.IncomingCall(
                dependencies.getUser(
                    uid = msg.uid,
                    currentUid = currentUid,
                )
            )
        )
        dependencies.callInfos.add(CallInfo(currentUid, msg.uid))
    }

    private suspend fun insertChatMessage(message: ChatMessage) {
        val finalMessage =
            dependencies.database
                .chatMessagesQueries
                .insertChatMessage(message)
        println("insertChatMessage ${Date().time.toDouble() / 1000} ${message.id} $finalMessage")
        userCreatedChatMessageInfosFlow.add(
            CreatedMessageInfo(
                tmpId = message.id,
                id = finalMessage.id
            )
        )
    }

    private fun chatMessageRead(messageId: ChatMessageId) {
        dependencies.database
            .run {
                transaction {
                    val message = chatMessagesQueries.getById(messageId).executeAsOne()
                    if (message.peerId != currentUid) {
                        println("ERROR: chatMessageRead currentUid: $currentUid message: $message")
                        return@transaction
                    }
                    lastReadMessagesQueries.run {
                        val lastReadMessageId = lastReadMessagesQueries
                            .selectMessageId(fromId = message.fromId, peerId = message.peerId)
                            .executeAsOneOrNull()
                        if (lastReadMessageId != null && lastReadMessageId >= messageId) {
                            return@transaction
                        }
                        if (lastReadMessageId != null) {
                            lastReadMessagesQueries
                                .delete(
                                    fromId = message.fromId,
                                    peerId = message.peerId,
                                )
                        }
                        lastReadMessagesQueries.insert(
                            fromId = message.fromId,
                            peerId = message.peerId,
                            messageId = messageId,
                        )
                    }
                }
            }
    }
}

private fun UsersFilter.Rating.toDoubleOrNull(): Double? =
    when (this) {
        UsersFilter.Rating.All -> null
        UsersFilter.Rating.Five -> 5.0
        UsersFilter.Rating.Four -> 4.0
        UsersFilter.Rating.Three -> 3.0
        UsersFilter.Rating.Two -> 2.0
        UsersFilter.Rating.One -> 1.0
    }

private fun Dependencies.callPartnerId(uid: UserId) =
    callInfos
        .withIndex()
        .firstOrNull { it.value.uids.contains(uid) }
        ?.run {
            IndexedValue(
                index,
                value
                    .uids
                    .first { it != uid }
            )
        }

suspend fun Dependencies.endCall(
    uid: UserId,
    reason: WebSocketMsg.Call.EndCall.Reason,
) = callPartnerId(uid)
    ?.run {
        connectedUserSessionsFlow[value]!!.send(WebSocketMsg.Call.EndCall(reason))
        callInfos.removeAt(index)
    }
