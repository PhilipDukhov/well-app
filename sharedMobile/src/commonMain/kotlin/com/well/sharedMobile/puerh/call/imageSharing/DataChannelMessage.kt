package com.well.sharedMobile.puerh.call.imageSharing

import com.well.serverModels.Date
import com.well.serverModels.Path
import com.well.serverModels.Size
import com.well.serverModels.prepareToDebug
import kotlinx.serialization.Serializable

@Serializable
sealed class DataChannelMessage {
    @Serializable
    data class InitiateSession(val date: Date) : DataChannelMessage()

    @Serializable
    object EndSession : DataChannelMessage()

    @Serializable
    data class UpdateViewSize(val size: Size) : DataChannelMessage()

    class UpdateImage(val imageData: ByteArray) : DataChannelMessage() {
        override fun toString() = super.toString().prepareToDebug()
    }

    @Serializable
    data class UpdatePaths(val paths: List<Path>) : DataChannelMessage()

    @Serializable
    object ConfirmUpdatePaths : DataChannelMessage()
}