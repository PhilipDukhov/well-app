package com.well.sharedMobile.puerh.call.imageSharing

import com.well.serverModels.Date
import com.well.serverModels.compareTo
import com.well.sharedMobile.puerh.call.imageSharing.ImageSharingFeature.Eff
import com.well.sharedMobile.puerh.call.imageSharing.ImageSharingFeature.Msg
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature.Msg as TopLevelMsg
import com.well.sharedMobile.puerh.call.resizedImageBase64Encoding
import com.well.utils.atomic.AtomicRef
import com.well.sharedMobile.utils.decodeBase64Image
import com.well.sharedMobile.puerh.call.WebRtcEffectHandler.SharingStateDataChannelMessage as RtcMsg

class ImageSharingEffectHandler(
    val webRtcSendListener: (RtcMsg) -> Unit,
) {
    private val initiatedSessionDate = AtomicRef<Date?>()

    fun handleEffect(eff: Eff)  {
        when (eff) {
            Eff.RequestImageUpdate -> Unit
            Eff.Init -> {
                val date = Date()
                initiatedSessionDate.value = date
                webRtcSendListener(RtcMsg.InitiateSession(date))
            }
            is Eff.NotifyViewSizeUpdate -> {
                webRtcSendListener(
                    RtcMsg.UpdateViewSize(
                        eff.size
                    )
                )
            }
            is Eff.UploadImage -> {
                webRtcSendListener(
                    RtcMsg.UpdateImage(
                        eff.resizedImageBase64Encoding()
                    )
                )
            }
            Eff.Close -> webRtcSendListener(RtcMsg.EndSession)
        }
    }

    fun handleDataChannelMessage(msg: RtcMsg) : TopLevelMsg? =
        TopLevelMsg.ImageSharingMsg(when (msg) {
            is RtcMsg.InitiateSession -> {
                initiatedSessionDate.value.let {
                    if (it != null) {
                        if (it > msg.date) {
                            Msg.SwitchToViewer
                        } else {
                            return null
                        }
                    } else {
                        return TopLevelMsg.StartImageSharing(ImageSharingFeature.State.Role.Viewer)
                    }
                }
            }
            RtcMsg.EndSession -> {
                Msg.Close
            }
            is RtcMsg.UpdateImage -> {
                Msg.RemoteUpdateImage(msg.imageData64String.decodeBase64Image())
            }
            is RtcMsg.UpdatePaths -> {
                Msg.UpdatePaths(msg.paths)
            }
            is RtcMsg.UpdateViewSize -> {
                Msg.UpdateRemoteViewSize(msg.size)
            }
        })
}