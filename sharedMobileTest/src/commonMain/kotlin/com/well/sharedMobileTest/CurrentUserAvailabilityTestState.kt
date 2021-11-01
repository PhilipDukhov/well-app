package com.well.sharedMobileTest

import com.well.modules.features.myProfile.myProfileFeature.currentUserAvailability.CurrentUserAvailabilitiesListFeature
import com.well.modules.models.Availability

// CurrentUserAvailabilitiesListFeature unused for easy readability
@Suppress("unused")
fun CurrentUserAvailabilitiesListFeature.testState() =
    CurrentUserAvailabilitiesListFeature.State(
        availabilities = Availability.testValues
    )