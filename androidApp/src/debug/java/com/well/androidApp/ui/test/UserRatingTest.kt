package com.well.androidApp.ui.test

import com.well.androidApp.ui.composableScreens.myProfile.RatingScreen
import com.well.modules.models.User
import androidx.compose.runtime.Composable

@Composable
fun UserRatingTest() {
    RatingScreen(
        user = User(
            id = 0,
            initialized = false,
            lastEdited = 0.0,
            favorite = false,
            fullName = "Philip Dukhov",
            type = User.Type.Expert,
            email = null,
            ratingInfo = User.RatingInfo(
                count = 0,
                average = 0.0,
                currentUserRating = null,
            ),
//            profileImageUrl = "https://well-images.s3.us-east-2.amazonaws.com/appImages/JaimeLandman.png",
            phoneNumber = null,
            countryCode = null,
            timeZoneIdentifier = null,
            credentials = null,
            academicRank = null,
            languages = setOf(),
            skills = setOf(),
            bio = null,
            education = null,
            professionalMemberships = null,
            publications = null,
            twitter = null,
            doximity = null
        ),
        maxCharacters = 150,
    ) {

    }
}