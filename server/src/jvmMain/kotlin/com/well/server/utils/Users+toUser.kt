package com.well.server.utils

import com.well.server.Users
import com.well.serverModels.User.Type.*
import com.well.serverModels.User

fun Users.toUser(): User =
    User(
        id,
        firstName,
        lastName,
        when {
            facebookId != null -> Facebook
            googleId != null -> Google
            testId != null -> Test
            else -> throw IllegalStateException()
        },
        profileImageUrl,
    )