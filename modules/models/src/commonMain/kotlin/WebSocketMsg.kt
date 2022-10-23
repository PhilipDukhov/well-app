package com.well.modules.models

import com.well.modules.models.chat.ChatMessage
import com.well.modules.models.chat.LastReadMessage
import kotlinx.serialization.Serializable

@Serializable
sealed class WebSocketMsg {
    @Serializable
    sealed class Front : WebSocketMsg() {
        @Serializable
        class InitiateCall(
            val uid: User.Id,
            val callId: CallId,
            val hasVideo: Boolean,
        ) : Front()

        @Serializable
        class IncomingCallReceived(
            val callId: CallId,
        ) : Front()

        @Serializable
        class SetExpertsFilter(
            val filter: UsersFilter?,
        ) : Front()

        @Serializable
        class SetUsersPresence(
            val usersPresence: List<UserPresenceInfo>,
        ) : Front()

        @Serializable
        class SetChatMessagePresence(
            val messagePresenceId: ChatMessage.Id,
        ) : Front()

        @Serializable
        class UpdateChatReadStatePresence(
            val lastReadMessages: List<LastReadMessage>,
        ) : Front()

        @Serializable
        class SetMeetingsPresence(
            val meetingsPresence: List<Pair<Meeting.Id, Meeting.State>>,
        ) : Front()

        @Serializable
        class ChatMessageRead(
            val messageId: ChatMessage.Id,
        ) : Front()

        @Serializable
        class CreateChatMessage(
            val message: ChatMessage,
        ) : Front()

        @Serializable
        class UpdateNotificationToken(
            val token: NotificationToken,
        ) : Front()

        @Serializable
        class UpdateMeetingState(
            val meetingId: Meeting.Id,
            val state: Meeting.State,
        ) : Front()

        @Serializable
        object Logout : Front()
    }

    @Serializable
    sealed class Back : WebSocketMsg() {
        @Serializable
        class UpdateUsers(
            val users: List<User>,
        ) : Back()

        @Serializable
        class ListFilteredExperts(
            val userIds: List<User.Id>,
        ) : Back()

        @Serializable
        class IncomingCall(
            val callId: CallId,
            override val user: User,
            override val hasVideo: Boolean,
        ) : Back(), CallInfo {
            override val id: CallId
                get() = callId
        }

        @Serializable
        class UpdateMessages(
            val messages: List<UpdateMessageInfo>,
        ) : Back() {
            @Serializable
            class UpdateMessageInfo(
                val message: ChatMessage,
                val tmpId: ChatMessage.Id? = null,
            )
        }

        @Serializable
        class UpdateCharReadPresence(
            val lastReadMessages: List<LastReadMessage>,
        ) : Back()

        @Serializable
        class AddMeetings(
            val meetings: List<Meeting>,
        ) : Back()

        @Serializable
        class RemovedMeetings(
            val ids: List<Meeting.Id>,
        ) : Back()

        @Serializable
        class RemovedUsers(
            val ids: List<User.Id>,
        ) : Back()

        @Serializable
        class OnlineUsersList(
            val ids: Set<User.Id>,
        ) : Back()

        @Serializable
        object NotificationTokenRequest : Back()
    }

    @Serializable
    sealed class Call : WebSocketMsg() {
        @Serializable
        class Offer(
            val sessionDescriptor: String,
        ) : Call() {
            override fun toString(): String =
                super.toString()
                    .prepareToDebug()
        }

        @Serializable
        class Answer(
            val sessionDescriptor: String,
        ) : Call() {
            override fun toString(): String =
                super.toString()
                    .prepareToDebug()
        }

        @Serializable
        class Candidate(
            val sdpMid: String,
            val sdpMLineIndex: Int,
            val sdp: String,
        ) : Call()

        @Serializable
        class EndCall(val reason: Reason) : Call() {
            @Serializable
            sealed class Reason {
                @Serializable
                object Offline : Reason()

                @Serializable
                object Decline : Reason()

                @Serializable
                object Busy : Reason()
            }
        }
    }
}