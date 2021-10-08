package com.well.androidApp.ui.test

import com.well.androidApp.ui.composableScreens.myProfile.RatingScreen
import com.well.modules.models.User
import com.well.sharedMobile.testData.testUser
import androidx.compose.runtime.Composable

@Composable
fun UserRatingTest() {
    RatingScreen(
        user = User.testUser,
        maxCharacters = 150,
    ) {

    }
}