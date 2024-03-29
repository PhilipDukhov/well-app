package com.well.modules.features.call.callFeature.webRtc

import com.well.modules.features.call.callFeature.CallFeature
import com.well.modules.models.DrawingPath
import com.well.modules.models.Size
import com.well.modules.models.date.Date
import com.well.modules.models.prepareToDebug
import kotlinx.serialization.Serializable

@Serializable
sealed class RtcMsg {
    @Serializable
    data class UpdateCaptureDimensions(val dimensions: Size) : RtcMsg()

    @Serializable
    data class UpdateDeviceState(val deviceState: RemoteDeviceState) : RtcMsg()

    @Serializable
    data class UpdateViewPoint(val viewPoint: CallFeature.State.ViewPoint) : RtcMsg()

    @Serializable
    data class ImageSharingContainer(val msg: Msg) : RtcMsg() {
        @Serializable
        sealed class Msg {
            @Serializable
            data class UpdateImageContainerSize(val size: Size) : Msg()

            @Serializable
            class UpdateImage(val imageData: ByteArray?) : Msg() {
                override fun toString() = "${super.toString().prepareToDebug()} ${imageData?.count()}"
            }

            @Serializable
            data class NotifyClear(val saveHistory: Boolean, val date: Date): Msg()

            @Serializable
            data class UpdatePaths(val paths: List<DrawingPath>) : Msg()

            @Serializable
            object ConfirmUpdatePaths : Msg()
        }
    }
}