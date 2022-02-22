package com.well.modules.utils.viewUtils.countryCodes

import com.well.modules.models.User
import com.well.modules.utils.kotlinUtils.spacedUppercaseName

fun User.AcademicRank.localizedDescription() = spacedUppercaseName()