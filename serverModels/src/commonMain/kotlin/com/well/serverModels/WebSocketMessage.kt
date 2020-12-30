package com.well.serverModels

import kotlinx.serialization.Serializable

@Serializable
sealed class WebSocketMessage {
    @Serializable
    data class OnlineUsersList(val users: List<User>): WebSocketMessage()

    @Serializable
    data class InitiateCall(
        val userId: UserId,
    ): WebSocketMessage()

    @Serializable
    data class IncomingCall(
        val user: User,
    ): WebSocketMessage()

    @Serializable
    data class Offer(
        val sessionDescriptor: String,
    ): WebSocketMessage()

    @Serializable
    data class Answer(
        val sessionDescriptor: String,
    ): WebSocketMessage()

    @Serializable
    data class Candidate(
        val sdpMid: String,
        val sdpMLineIndex: Int,
        val sdp: String,
    ): WebSocketMessage()

    @Serializable
    data class EndCall(val reason: Reason): WebSocketMessage() {
        @Serializable
        sealed class Reason {
            @Serializable
            object Offline: Reason()

            @Serializable
            object Decline: Reason()

            @Serializable
            object Busy: Reason()
        }
    }
}