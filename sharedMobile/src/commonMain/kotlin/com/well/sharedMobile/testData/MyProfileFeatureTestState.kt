package com.well.sharedMobile.testData

import com.well.modules.models.User
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature.State

fun MyProfileFeature.testState(isCurrent: Boolean) = initialState(isCurrent = isCurrent, user = User.testUser)
    .copy(editingStatus = if (isCurrent) State.EditingStatus.Editing else State.EditingStatus.Preview)