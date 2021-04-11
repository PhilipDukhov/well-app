package com.well.sharedMobile.puerh.call.webRtc

import com.well.sharedMobile.puerh.call.CallFeature
import com.well.modules.utils.platform.Platform
import com.well.modules.utils.platform.isDebug

data class LocalDeviceState(
    val micEnabled: Boolean,
    val cameraEnabled: Boolean,
    val audioSpeakerEnabled: Boolean,
    val isFrontCamera: Boolean,
) {
    fun toggleMicMsg(): CallFeature.Msg =
        CallFeature.Msg.SetMicEnabled(!micEnabled)

    fun toggleCameraMsg(): CallFeature.Msg =
        CallFeature.Msg.SetCameraEnabled(!cameraEnabled)

    fun toggleAudioSpeakerMsg(): CallFeature.Msg =
        CallFeature.Msg.SetAudioSpeakerEnabled(!audioSpeakerEnabled)

    fun toggleIsFrontCameraMsg(): CallFeature.Msg =
        CallFeature.Msg.SetIsFrontCamera(!isFrontCamera)

    companion object {
        val default = LocalDeviceState(
            micEnabled = true,
            cameraEnabled = true,
            audioSpeakerEnabled = !Platform.isDebug,
            isFrontCamera = true
        )
    }
}