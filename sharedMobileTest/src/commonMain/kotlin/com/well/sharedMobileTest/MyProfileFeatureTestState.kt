package com.well.sharedMobileTest

import com.well.modules.models.User
import com.well.modules.features.myProfile.myProfileFeature.MyProfileFeature
import com.well.modules.features.myProfile.myProfileFeature.MyProfileFeature.State

fun MyProfileFeature.testState(isCurrent: Boolean) = initialState(isCurrent = isCurrent, user = User.testUser)
    .copy(
        editingStatus = State.EditingStatus.Preview
    )