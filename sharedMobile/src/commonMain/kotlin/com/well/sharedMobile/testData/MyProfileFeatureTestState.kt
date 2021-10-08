package com.well.sharedMobile.testData

import com.well.modules.models.User
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature

fun MyProfileFeature.testState() = initialState(isCurrent = true, user = User.testUser)
    .copy(editingStatus = MyProfileFeature.State.EditingStatus.Editing)