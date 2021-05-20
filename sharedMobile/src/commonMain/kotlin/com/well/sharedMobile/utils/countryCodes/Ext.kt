package com.well.sharedMobile.utils.countryCodes

import com.well.modules.models.User
import com.well.modules.models.spacedUppercaseName

fun User.AcademicRank.localizedDescription() = spacedUppercaseName()