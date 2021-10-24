package com.well.modules.features.call.callFeature.webRtc

import com.well.modules.features.call.callFeature.webRtc.LocalDeviceState
import kotlinx.serialization.Serializable

@Serializable
data class RemoteDeviceState(
    val micEnabled: Boolean,
    val cameraEnabled: Boolean,
) {
    constructor(deviceState: LocalDeviceState) : this(
        micEnabled = deviceState.micEnabled,
        cameraEnabled = deviceState.cameraEnabled,
    )
}