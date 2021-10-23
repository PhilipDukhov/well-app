package com.well.sharedMobileTest

import com.well.modules.models.Availability
import com.well.modules.models.Repeat
import com.well.modules.models.date.dateTime.daysShift
import com.well.modules.models.date.dateTime.time
import com.well.modules.features.myProfile.currentUserAvailability.CurrentUserAvailabilitiesListFeature
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration

// CurrentUserAvailabilitiesListFeature unused for easy readability
@Suppress("unused")
fun CurrentUserAvailabilitiesListFeature.testState() =
    CurrentUserAvailabilitiesListFeature.State(
        availabilities = Availability.testValues
    )