package com.well.modules.features.call.callFeature.webRtc

import com.well.modules.atomic.freeze
import com.well.modules.features.call.callFeature.CallFeature
import com.well.modules.utils.viewUtils.platform.Platform
import com.well.modules.utils.viewUtils.platform.isDebug

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
        fun default(cameraEnabled: Boolean? = null) =
            LocalDeviceState(
                micEnabled = true,
                cameraEnabled = cameraEnabled ?: true,
                audioSpeakerEnabled = !Platform.isDebug,
                isFrontCamera = true
            ).freeze()
    }
}