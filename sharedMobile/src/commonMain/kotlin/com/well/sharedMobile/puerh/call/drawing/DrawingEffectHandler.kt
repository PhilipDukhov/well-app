package com.well.sharedMobile.puerh.call.drawing

import com.well.serverModels.Path
import com.well.sharedMobile.puerh.call.CallFeature
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature.Eff
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature.Msg
import com.well.sharedMobile.puerh.call.resizedImage
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature.Msg as TopLevelMsg
import com.well.sharedMobile.utils.asImageContainer
import com.well.utils.atomic.AtomicRef
import com.well.sharedMobile.puerh.call.webRtc.RtcMsg.ImageSharingContainer.Msg as RtcMsg

class DrawingEffectHandler(
    val webRtcSendListener: (RtcMsg) -> Unit,
) {
    private val waitingForPathConfirmation = AtomicRef(false)
    private val pendingPaths = AtomicRef<List<Path>?>()

    fun handleEffect(eff: Eff) {
        println("ImageSharingEffectHandler handleEffect $eff")
        when (eff) {
            is Eff.NotifyViewSizeUpdate -> {
                webRtcSendListener(
                    RtcMsg.UpdateViewSize(
                        eff.size
                    )
                )
            }
            is Eff.UploadImage -> {
                val image = eff.resizedImage()
                webRtcSendListener(
                    RtcMsg.UpdateImage(
                        image.asByteArray(0.3F)
                    )
                )
                webRtcSendListener(
                    RtcMsg.UpdateImage(
                        image.asByteArray(1F)
                    )
                )
            }
            is Eff.UploadPaths -> {
                if (!waitingForPathConfirmation.value) {
                    waitingForPathConfirmation.value = true
                    pendingPaths.value = null
                    webRtcSendListener(RtcMsg.UpdatePaths(eff.paths))
                    println("apthssss: uploding ${eff.paths}")
                } else {
                    pendingPaths.value = eff.paths
                }
            }
        }
    }

    fun handleDataChannelMessage(msg: RtcMsg): TopLevelMsg? =
        TopLevelMsg.CallMsg(CallFeature.Msg.DrawingMsg(when (msg) {
            is RtcMsg.UpdateImage -> {
                Msg.RemoteUpdateImage(msg.imageData.asImageContainer())
            }
            is RtcMsg.UpdatePaths -> {
                println("apthssss: get ${msg.paths}")
                webRtcSendListener(RtcMsg.ConfirmUpdatePaths)
                Msg.UpdatePaths(msg.paths)
            }
            is RtcMsg.UpdateViewSize -> {
                Msg.UpdateRemoteViewSize(msg.size)
            }
            RtcMsg.ConfirmUpdatePaths -> {
                waitingForPathConfirmation.value = false
                val pendingPaths = pendingPaths.value
                if (pendingPaths != null) {
                    handleEffect(Eff.UploadPaths(pendingPaths))
                }
                run {
                    return null
                }
            }
        })).also {
            println("ImageSharingEffectHandler handleDataChannelMessage $msg $it")
        }
}