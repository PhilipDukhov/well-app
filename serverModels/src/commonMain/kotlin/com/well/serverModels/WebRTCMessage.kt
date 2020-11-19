package com.well.serverModels

import kotlinx.serialization.Serializable

@Serializable
sealed class WebRTCMessage {
    @Serializable
    object Created : WebRTCMessage()

    @Serializable
    object Join : WebRTCMessage()

    @Serializable
    object Joined : WebRTCMessage()

    @Serializable
    data class Offer(val sdp: String) : WebRTCMessage()

    @Serializable
    data class Answer(val sdp: String) : WebRTCMessage()

    @Serializable
    data class Candidate(val id: String, val label: Int, val candidate: String) : WebRTCMessage()
}