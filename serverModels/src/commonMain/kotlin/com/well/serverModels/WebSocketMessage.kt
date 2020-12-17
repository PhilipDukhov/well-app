package com.well.serverModels

import kotlinx.serialization.Serializable

@Serializable
sealed class WebSocketMessage {
    @Serializable
    data class OnlineUsersList(val users: List<User>): WebSocketMessage()

    @Serializable
    data class InitiateCall(
        val userId: UserId,
        val webRTCSessionDescriptor: String,
    ): WebSocketMessage()

    @Serializable
    data class AcceptCall(
        val webRTCSessionDescriptor: String,
    ): WebSocketMessage()

    @Serializable
    data class EndCall(val reason: Reason): WebSocketMessage() {
        @Serializable
        sealed class Reason {
            @Serializable
            object Offline: Reason()

            @Serializable
            object Decline: Reason()
        }
    }
}