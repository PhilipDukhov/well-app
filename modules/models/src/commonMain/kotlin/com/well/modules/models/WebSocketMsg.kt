package com.well.modules.models

import kotlinx.serialization.Serializable

@Serializable
sealed class WebSocketMsg {
    @Serializable
    data class CurrentUser(
        val user: User,
    ) : WebSocketMsg()

    // Call
    @Serializable
    data class InitiateCall(
        val uid: UserId,
    ) : WebSocketMsg()

    @Serializable
    data class IncomingCall(
        val user: User,
    ) : WebSocketMsg()

    @Serializable
    data class Offer(
        val sessionDescriptor: String,
    ) : WebSocketMsg() {
        override fun toString(): String =
            super.toString()
                .prepareToDebug()
    }

    @Serializable
    data class Answer(
        val sessionDescriptor: String,
    ) : WebSocketMsg() {
        override fun toString(): String =
            super.toString()
                .prepareToDebug()
    }

    @Serializable
    data class Candidate(
        val sdpMid: String,
        val sdpMLineIndex: Int,
        val sdp: String,
    ) : WebSocketMsg()

    @Serializable
    data class EndCall(val reason: Reason) : WebSocketMsg() {
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