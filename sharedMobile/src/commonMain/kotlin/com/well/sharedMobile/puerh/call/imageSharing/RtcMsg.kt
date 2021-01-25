package com.well.sharedMobile.puerh.call.imageSharing

import com.well.serverModels.Date
import com.well.serverModels.Path
import com.well.serverModels.Size
import com.well.serverModels.prepareToDebug
import com.well.sharedMobile.puerh.call.webRtc.RemoteDeviceState
import kotlinx.serialization.Serializable

@Serializable
sealed class RtcMsg {
    @Serializable
    data class UpdateDeviceState(val deviceState: RemoteDeviceState) : RtcMsg()

    @Serializable
    data class ImageSharingContainer(val msg: Msg) : RtcMsg() {
        @Serializable
        sealed class Msg {
            @Serializable
            data class InitiateSession(val date: Date) : Msg()

            @Serializable
            object EndSession : Msg()

            @Serializable
            data class UpdateViewSize(val size: Size) : Msg()

            class UpdateImage(val imageData: ByteArray) : Msg() {
                override fun toString() = super.toString().prepareToDebug()
            }

            @Serializable
            data class UpdatePaths(val paths: List<Path>) : Msg()

            @Serializable
            object ConfirmUpdatePaths : Msg()
        }
    }
}