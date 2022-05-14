package com.well.server.routing

import com.well.modules.db.server.LastReadMessages
import com.well.modules.db.server.Meetings
import com.well.modules.db.server.filterFlow
import com.well.modules.db.server.getAllForUserFlow
import com.well.modules.db.server.getByIdsFlow
import com.well.modules.db.server.getByUserIdFlow
import com.well.modules.db.server.insertChatMessage
import com.well.modules.db.server.listByOwnerIdFlow
import com.well.modules.db.server.peerIdsListFlow
import com.well.modules.db.server.selectByAnyIdFlow
import com.well.modules.db.server.selectByDeviceIdFlow
import com.well.modules.db.server.toChatMessage
import com.well.modules.db.server.toLastReadMessage
import com.well.modules.db.server.toMeeting
import com.well.modules.db.server.toUser
import com.well.modules.models.DeviceId
import com.well.modules.models.Meeting
import com.well.modules.models.User
import com.well.modules.models.UserPresenceInfo
import com.well.modules.models.UsersFilter
import com.well.modules.models.WebSocketMsg
import com.well.modules.models.chat.ChatMessage
import com.well.modules.models.chat.LastReadMessage
import com.well.modules.utils.dbUtils.adaptedOneOfRegex
import com.well.modules.utils.flowUtils.MutableSetStateFlow
import com.well.modules.utils.flowUtils.asSingleFlow
import com.well.modules.utils.flowUtils.collectIn
import com.well.modules.utils.flowUtils.combineToSet
import com.well.modules.utils.flowUtils.filterIterable
import com.well.modules.utils.flowUtils.filterNotEmpty
import com.well.modules.utils.flowUtils.flatMapLatest
import com.well.modules.utils.flowUtils.mapIterable
import com.well.modules.utils.flowUtils.mapPair
import com.well.server.utils.CallInfo
import com.well.server.utils.ClientKey
import com.well.server.utils.Services
import com.well.server.utils.send
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import org.joda.time.Weeks

class UserSession(
    private val currentUid: User.Id,
    private val deviceId: DeviceId,
    private val webSocketSession: WebSocketSession,
    private val services: Services,
) : WebSocketSession by webSocketSession {
    private class CreatedMessageInfo(
        val tmpId: ChatMessage.Id,
        val id: ChatMessage.Id,
    )

    private val expertsFilterFlow = MutableStateFlow<UsersFilter?>(null)
    private val usersPresenceInfoFlow = MutableStateFlow<List<UserPresenceInfo>?>(null)
    private val messagesPresenceInfoFlow = MutableStateFlow<ChatMessage.Id?>(null)
    private val meetingIdsPresenceFlow = MutableStateFlow<List<Pair<Meeting.Id, Meeting.State>>?>(null)
    private val chatReadStatePresenceFlow = MutableStateFlow<List<LastReadMessage>>(emptyList())
    private val userCreatedChatMessageInfosFlow = MutableSetStateFlow<CreatedMessageInfo>()
    private val currentClientKey = ClientKey(deviceId, currentUid)

    private val expertsFlow: Flow<List<User.Id>> = expertsFilterFlow
        .filterNotNull()
        .flatMapLatest { filter ->
            val maxLastOnlineDistance =
                if (!filter.activity.contains(UsersFilter.Activity.LastWeek)) 0 else
                    Weeks.ONE.toStandardSeconds().seconds.toLong()
            val specificIdsRegexpFlow =
                if (!filter.activity.contains(UsersFilter.Activity.Now)) "".asSingleFlow() else
                    services
                        .connectedUserSessionsFlow
                        .map { connectedUserSessions ->
                            connectedUserSessions
                                .map { it.key.toString() }
                                .adaptedOneOfRegex()
                        }

            specificIdsRegexpFlow.flatMapLatest { specificIdsRegexp ->
                services.database.usersQueries
                    .filterFlow(
                        uid = currentUid,
                        maxLastOnlineDistance = maxLastOnlineDistance,
                        specificIdsRegexp = specificIdsRegexp,
                        filter = filter
                    )
            }
        }
    private val newMessagesFlow =
        messagesPresenceInfoFlow
            .filterNotNull()
            .combine(userCreatedChatMessageInfosFlow) { messagesPresenceInfo, userCreatedChatMessageInfos ->
                services.database.chatMessagesQueries
                    .getAllForUserFlow(
                        id = currentUid,
                        lastPresentedId = messagesPresenceInfo,
                    )
                    .mapIterable { message ->
                        WebSocketMsg.Back.UpdateMessages.UpdateMessageInfo(
                            message.toChatMessage(services.database),
                            tmpId = userCreatedChatMessageInfos.firstOrNull { it.id == message.id }?.tmpId,
                        )
                    }
            }
            .flatMapLatest()
            .filterNotEmpty()
    private val newReadStatesFlow =
        chatReadStatePresenceFlow
            .flatMapLatest { chatReadStatePresence ->
                val chatReadStatePresenceMap = chatReadStatePresence
                    .groupBy { it.fromId to it.peerId }
                    .mapValues { it.value.first() }
                services.database.lastReadMessagesQueries
                    .selectByAnyIdFlow(currentUid)
                    .filterIterable { databaseLRM ->
                        val currentReadState =
                            chatReadStatePresenceMap[databaseLRM.fromId to databaseLRM.peerId]
                                ?: return@filterIterable true
                        databaseLRM.messageId > currentReadState.messageId
                    }
                    .mapIterable(LastReadMessages::toLastReadMessage)
            }
            .filterNotEmpty()

    private val meetingsPresenceFlow: Flow<Pair<List<Pair<Meeting.Id, Meeting.State>>, List<Meetings>>> =
        meetingIdsPresenceFlow
            .filterNotNull()
            .combine(
                services.database.meetingsQueries
                    .getByUserIdFlow(currentUid)
            ) { ids, meetings ->
                ids to meetings
            }

    private val newMeetingsFlow: Flow<List<Meeting>> = meetingsPresenceFlow
        .mapPair { presenceIds, dbMeetings ->
            dbMeetings.filter {
                !presenceIds.contains(it.id to it.state) && !it.deleted
            }
        }
        .mapIterable(Meetings::toMeeting)
        .filterNotEmpty()
    private val removedMeetingIdsFlow: Flow<List<Meeting.Id>> = meetingsPresenceFlow
        .mapPair { presenceIds, dbMeetings ->
            dbMeetings.filter { dbMeeting ->
                presenceIds.any { it.first == dbMeeting.id } && dbMeeting.deleted
            }
        }
        .mapIterable(Meetings::id)
        .filterNotEmpty()

    private val neededUsersFlow = expertsFlow
        .combineToSet(flowOf(setOf(currentUid)))
        .combineToSet(
            services.database.chatMessagesQueries
                .peerIdsListFlow(id = currentUid)
        )
        .combineToSet(
            services.database.favoritesQueries
                .listByOwnerIdFlow(currentUid)
        )
        .combineToSet(
            services.database.meetingsQueries
                .getByUserIdFlow(currentUid)
                .mapIterable { setOf(it.expertUid, it.creatorUid) }
                .map { it.flatten() }
        )
    private val notUpToDateUsersFlow =
        usersPresenceInfoFlow
            .filterNotNull()
            .combine(neededUsersFlow) { usersPresenceInfo, neededUsers ->
                services.database
                    .usersQueries
                    .getByIdsFlow(neededUsers)
                    .map { users ->
                        users.filter { user ->
                            usersPresenceInfo
                                .firstOrNull { it.id == user.id }
                                ?.lastEdited?.let { presenceLastEdited ->
                                    user.lastEdited > presenceLastEdited
                                } ?: true
                        }.map { it.toUser(currentUid, services.database) }
                    }
            }
            .flatMapLatest()
            .filterNotEmpty()
    private val removedUsersFlow = usersPresenceInfoFlow
        .filterNotNull()
        .flatMapLatest { usersPresenceInfo ->
            val usersPresenceIds = usersPresenceInfo.map { it.id }
            services.database.usersQueries
                .getByIdsFlow(usersPresenceIds)
                .map { foundUsers ->
                    val foundUserIds = foundUsers.map { it.id }
                    usersPresenceIds
                        .filterNot(foundUserIds::contains)
                }
        }
        .filterNotEmpty()
    private val fcmTokenNeededFlow = services.database
        .notificationTokensQueries
        .selectByDeviceIdFlow(deviceId = deviceId)
        .filter { token ->
            token == null
                    || token.timestamp.daysUntil(Clock.System.now(), TimeZone.currentSystemDefault()) > 10
        }.map {}
    private val onlineUsersFlow = neededUsersFlow
        .combine(services.connectedUserSessionsFlow) { neededUsers, connectedUserSessions ->
            neededUsers.filter { uid ->
                uid != currentUid && connectedUserSessions.any { it.key.uid == uid }
            }.toSet()
        }
        .distinctUntilChanged()

    init {
        listOf(
            expertsFlow.map(WebSocketMsg.Back::ListFilteredExperts),
            notUpToDateUsersFlow.map(WebSocketMsg.Back::UpdateUsers),
            newMessagesFlow.map(WebSocketMsg.Back::UpdateMessages),
            newReadStatesFlow.map(WebSocketMsg.Back::UpdateCharReadPresence),
            newMeetingsFlow.map(WebSocketMsg.Back::AddMeetings),
            removedMeetingIdsFlow.map(WebSocketMsg.Back::RemovedMeetings),
            fcmTokenNeededFlow.map { WebSocketMsg.Back.NotificationTokenRequest },
            removedUsersFlow.map(WebSocketMsg.Back::RemovedUsers),
            onlineUsersFlow.map(WebSocketMsg.Back::OnlineUsersList)
        ).forEach { flow ->
            flow
                .onEach {
                    println("$currentUid send $it")
                }
                .collectIn(this, ::send)
        }
    }

    suspend fun handleCallMsg(msg: WebSocketMsg.Call) {
        when (msg) {
            is WebSocketMsg.Call.Answer,
            is WebSocketMsg.Call.Candidate,
            is WebSocketMsg.Call.Offer,
            -> {
                val indexedClientKey = services.callPartnerId(currentUid)!!
                services.connectedUserSessionsFlow
                    .value[indexedClientKey.value]
                    ?.send(msg)
            }
            is WebSocketMsg.Call.EndCall -> {
                services.endCall(
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
                expertsFilterFlow.value = msg.filter
            }
            is WebSocketMsg.Front.SetUsersPresence -> {
                usersPresenceInfoFlow.value = msg.usersPresence
            }
            is WebSocketMsg.Front.InitiateCall -> {
                initiateCall(msg)
            }
            is WebSocketMsg.Front.CreateChatMessage -> {
                insertChatMessage(msg.message)
            }
            is WebSocketMsg.Front.SetChatMessagePresence -> {
                messagesPresenceInfoFlow.value = msg.messagePresenceId
                services.pendingNotificationIds
                    .removeAll {
                        it.itemId is Services.PendingNotificationId.ItemId.ChatMessage &&
                                it.deviceId == deviceId &&
                                it.itemId.chatMessageId <= msg.messagePresenceId
                    }
            }
            is WebSocketMsg.Front.ChatMessageRead -> {
                chatMessageRead(msg.messageId)
            }
            is WebSocketMsg.Front.UpdateChatReadStatePresence -> {
                services.database.run {
                    transaction {
                        val newLastReads = msg.lastReadMessages
                            .filter { it.peerId == currentUid }
                        if (newLastReads.isEmpty()) return@transaction
                        val currentLastReads = lastReadMessagesQueries.select(
                            fromAndPeerIds = newLastReads
                                .map { "${it.fromId}|$currentUid" }
                        ).executeAsList()
                            .groupBy { it.fromId }.mapValues { it.value.first() }
                        newLastReads.forEach { newLastRead ->
                            val currentLastRead = currentLastReads[newLastRead.fromId]
                            if (currentLastRead != null && currentLastRead.messageId >= newLastRead.messageId) {
                                return@forEach
                            }
                            if (currentLastRead != null) {
                                lastReadMessagesQueries.delete(
                                    fromId = newLastRead.fromId,
                                    peerId = newLastRead.peerId,
                                )
                            }
                            lastReadMessagesQueries.insert(
                                LastReadMessages(
                                    fromId = newLastRead.fromId,
                                    peerId = newLastRead.peerId,
                                    messageId = newLastRead.messageId,
                                )
                            )
                        }
                    }
                }
                chatReadStatePresenceFlow.value = msg.lastReadMessages
            }
            is WebSocketMsg.Front.SetMeetingsPresence -> {
                meetingIdsPresenceFlow.value = msg.meetingsPresence
                services.pendingNotificationIds
                    .removeAll { pendingId ->
                        pendingId.itemId is Services.PendingNotificationId.ItemId.Meeting &&
                                pendingId.deviceId == deviceId &&
                                msg.meetingsPresence.any { it.first == pendingId.itemId.meetingId }
                    }
            }
            is WebSocketMsg.Front.Logout -> {
                services.database.notificationTokensQueries.delete(deviceId)
            }
            is WebSocketMsg.Front.UpdateNotificationToken -> {
                services.database.notificationTokensQueries.insert(
                    token = msg.token,
                    deviceId = deviceId,
                    uid = currentUid,
                    timestamp = Clock.System.now(),
                )
            }
            is WebSocketMsg.Front.UpdateMeetingState -> {
                val dbMeeting = services.database.meetingsQueries.getById(msg.meetingId).executeAsOne()
                if (dbMeeting.state != Meeting.State.Requested && msg.state !is Meeting.State.Canceled) {
                    throw IllegalStateException("Meetings state cannot be updated from ${dbMeeting.state} to ${msg.state}")
                }
                val cancelledState = msg.state as? Meeting.State.Canceled
                if (cancelledState != null) {
                    services.database.meetingsQueries.markDeleted(dbMeeting.id)

                    when (dbMeeting.state) {
                        is Meeting.State.Requested,
                        is Meeting.State.Rejected,
                        -> return
                        else -> Unit
                    }

                    val meeting = dbMeeting.toMeeting()
                    val messageId = services.database.insertChatMessage(
                        fromId = currentUid,
                        peerId = meeting.otherUid(currentUid),
                        content = ChatMessage.Content.Text("I've cancelled our meeting ${meeting.dateTimeDescription} because: ${cancelledState.reason}")
                    )
                    services.deliverMessageNotification(messageId)
                } else {
                    services.database.meetingsQueries.updateState(id = msg.meetingId, state = msg.state)
                    services.deliverMeetingNotification(msg.meetingId, currentUid)
                }
            }
        }
    }

    private suspend fun initiateCall(msg: WebSocketMsg.Front.InitiateCall) {
        val session = services.connectedUserSessionsFlow.value.entries.firstOrNull { it.key.uid == msg.uid }
        if (session == null) {
            send(WebSocketMsg.Call.EndCall(WebSocketMsg.Call.EndCall.Reason.Offline))
            return
        }
        if (services.callInfos.any { it.uids.contains(currentClientKey) }) {
            send(WebSocketMsg.Call.EndCall(WebSocketMsg.Call.EndCall.Reason.Busy))
            return
        }
        session.value.send(
            WebSocketMsg.Back.IncomingCall(
                services.getUser(
                    uid = currentUid,
                    currentUid = msg.uid,
                )
            )
        )
        services.callInfos.add(CallInfo(listOf(currentClientKey, session.key)))
    }

    private fun insertChatMessage(message: ChatMessage) {
        with(services.database) {
            val id = transactionWithResult<ChatMessage.Id> {
                val finalMessageId = insertChatMessage(message)
                userCreatedChatMessageInfosFlow.add(
                    CreatedMessageInfo(
                        tmpId = message.id,
                        id = finalMessageId
                    )
                )
                finalMessageId
            }
            services.deliverMessageNotification(id)
        }
    }

    private fun chatMessageRead(messageId: ChatMessage.Id) {
        services.database
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
                            LastReadMessages(
                                fromId = message.fromId,
                                peerId = message.peerId,
                                messageId = messageId,
                            )
                        )
                    }
                }
            }
    }
}

private fun Services.callPartnerId(uid: User.Id) =
    callInfos
        .withIndex()
        .firstOrNull { it.value.uids.any { it.uid == uid } }
        ?.run {
            IndexedValue(
                index,
                value
                    .uids
                    .first { it.uid != uid }
            )
        }

suspend fun Services.endCall(
    uid: User.Id,
    reason: WebSocketMsg.Call.EndCall.Reason,
) = callPartnerId(uid)
    ?.run {
        connectedUserSessionsFlow[value]!!.send(WebSocketMsg.Call.EndCall(reason))
        callInfos.removeAt(index)
    }