package com.well.sharedMobile.puerh.call.webRtc

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