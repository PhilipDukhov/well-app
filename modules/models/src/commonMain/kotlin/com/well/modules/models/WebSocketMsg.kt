package com.well.modules.models

import com.well.modules.models.chat.ChatMessage
import com.well.modules.models.chat.LastReadMessage
import kotlinx.serialization.Serializable

@Serializable
sealed class WebSocketMsg {
    @Serializable
    sealed class Front: WebSocketMsg() {
        @Serializable
        data class InitiateCall(
            val uid: UserId,
        ) : Front()

        @Serializable
        data class SetExpertsFilter(
            val filter: UsersFilter?,
        ) : Front()

        @Serializable
        data class SetUsersPresence(
            val usersPresence: List<UserPresenceInfo>,
        ) : Front()

        @Serializable
        data class SetChatMessagePresence(
            val messagePresenceId: ChatMessageId,
        ) : Front()

        @Serializable
        data class UpdateChatReadStatePresence(
            val lastReadMessages: List<LastReadMessage>,
        ) : Front()

        @Serializable
        data class ChatMessageRead(
            val messageId: ChatMessageId,
        ) : Front()

        @Serializable
        data class CreateChatMessage(
            val message: ChatMessage,
        ) : Front()
    }

    @Serializable
    sealed class Back: WebSocketMsg() {
        @Serializable
        data class UpdateUsers(
            val users: List<User>,
        ) : Back()

        @Serializable
        data class ListFilteredExperts(
            val userIds: List<UserId>,
        ) : Back()

        @Serializable
        data class IncomingCall(
            val user: User,
        ) : Back()

        @Serializable
        data class UpdateMessages(
            val messages: List<UpdateMessageInfo>,
        ) : Back() {
            @Serializable
            data class UpdateMessageInfo(
                val message: ChatMessage,
                val tmpId: UserId? = null,
            )
        }

        @Serializable
        data class UpdateCharReadPresence(
            val lastReadMessages: List<LastReadMessage>,
        ) : Back()
    }

    sealed class Call: WebSocketMsg() {
        @Serializable
        data class Offer(
            val sessionDescriptor: String,
        ) : Call() {
            override fun toString(): String =
                super.toString()
                    .prepareToDebug()
        }

        @Serializable
        data class Answer(
            val sessionDescriptor: String,
        ) : Call() {
            override fun toString(): String =
                super.toString()
                    .prepareToDebug()
        }

        @Serializable
        data class Candidate(
            val sdpMid: String,
            val sdpMLineIndex: Int,
            val sdp: String,
        ) : Call()

        @Serializable
        data class EndCall(val reason: Reason) : Call() {
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