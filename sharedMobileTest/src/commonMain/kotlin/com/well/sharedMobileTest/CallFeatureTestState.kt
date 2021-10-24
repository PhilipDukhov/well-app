package com.well.sharedMobileTest

import com.well.modules.features.call.callFeature.CallFeature
import com.well.modules.models.date.Date
import com.well.modules.models.Size
import com.well.modules.models.User

private val testDate = Date()

fun CallFeature.testState(status: CallFeature.State.Status) =
    callingStateAndEffects(User.testUser)
        .first.run {
            copy(
                status = status,
                callStartedDateInfo = CallFeature.State.CallStartedDateInfo(testDate),
                localDeviceState = localDeviceState.copy(cameraEnabled = false),
                localCaptureDimensions = Size(1080, 1920),
                remoteCaptureDimensions = Size(1080, 1920),
            )
        }