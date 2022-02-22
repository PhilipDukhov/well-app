package com.well.sharedMobileTest

import com.well.modules.features.more.moreFeature.subfeatures.FavoritesFeature
import com.well.modules.models.User

fun FavoritesFeature.testState() =
    FavoritesFeature.State(
        (listOf(
            User.testUser,
            User.testUser.copy(fullName = "David Test"),
            User.testUser.copy(fullName = "Test David"),
            User.testUser.copy(fullName = "Durov Philip"),
        ) + List(20) {User.testUser})
            .let {
                var id = 0L
                it.map {
                    it.copy(id = User.Id(id++))
                }
            }
    )