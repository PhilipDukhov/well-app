package com.well.sharedMobileTest

import com.well.modules.features.myProfile.myProfileFeature.availabilitiesCalendar.AvailabilitiesCalendarFeature
import com.well.modules.models.Availability

// AvailabilitiesCalendarFeature unused for easy readability
@Suppress("unused")
fun AvailabilitiesCalendarFeature.testState() =
    AvailabilitiesCalendarFeature.State(
        availabilities = Availability.testValues
    )