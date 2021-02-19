package com.well.sharedMobile.puerh.call.drawing

import com.well.serverModels.Path
import com.well.sharedMobile.puerh.call.CallFeature
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature.Eff
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature.Msg
import com.well.sharedMobile.puerh.call.resizedImage
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature.Msg as TopLevelMsg
import com.well.sharedMobile.utils.asImageContainer
import com.well.atomic.AtomicRef
import com.well.napier.Napier
import com.well.sharedMobile.puerh.call.webRtc.RtcMsg.ImageSharingContainer.Msg as RtcMsg

class DrawingEffectHandler(
    val webRtcSendListener: (RtcMsg) -> Unit,
) {
    private val waitingForPathConfirmation = AtomicRef(false)
    private val pendingPaths = AtomicRef<List<Path>?>()

    fun handleEffect(eff: Eff) {
        when (eff) {
            is Eff.RequestImageUpdate,
            -> Unit
            is Eff.NotifyViewSizeUpdate -> {
                webRtcSendListener(
                    RtcMsg.UpdateImageContainerSize(
                        eff.size
                    )
                )
            }
            is Eff.UploadImage -> {
                val image = eff.image.resizedImage(eff.remoteViewSize)
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
            is Eff.ClearImage -> {
                webRtcSendListener(
                    RtcMsg.UpdateImage(null)
                )
            }
            is Eff.UploadPaths -> {
                if (!waitingForPathConfirmation.value) {
                    waitingForPathConfirmation.value = true
                    pendingPaths.value = null
                    webRtcSendListener(RtcMsg.UpdatePaths(eff.paths))
                } else {
                    pendingPaths.value = eff.paths
                }
            }
            is Eff.NotifyClear -> {
                webRtcSendListener(
                    RtcMsg.NotifyClear(eff.saveHistory, eff.date)
                )
            }
        }
    }

    fun handleDataChannelMessage(msg: RtcMsg): TopLevelMsg? =
        TopLevelMsg.CallMsg(CallFeature.Msg.DrawingMsg(when (msg) {
            is RtcMsg.UpdateImage -> {
                Msg.RemoteUpdateImage(msg.imageData?.asImageContainer())
            }
            is RtcMsg.UpdatePaths -> {
                webRtcSendListener(RtcMsg.ConfirmUpdatePaths)
                Msg.UpdatePaths(msg.paths)
            }
            is RtcMsg.UpdateImageContainerSize -> {
                Msg.UpdateRemoteImageContainerSize(msg.size)
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
            is RtcMsg.NotifyClear -> {
                Msg.RemoteClear(msg.saveHistory, msg.date)
            }
        })).also {
            Napier.i("ImageSharingEffectHandler handleDataChannelMessage $msg $it")
        }
}