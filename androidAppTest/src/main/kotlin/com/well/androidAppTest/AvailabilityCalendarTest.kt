package com.well.androidAppTest

import com.well.modules.androidUi.composableScreens.myProfile.availability.CurrentUserAvailabilityView
import com.well.modules.features.myProfile.myProfileFeature.availabilitiesCalendar.AvailabilitiesCalendarFeature
import com.well.sharedMobileTest.testState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.accompanist.insets.systemBarsPadding

@Composable
fun AvailabilityCalendarTest() {
    val state = remember {
        mutableStateOf(
            AvailabilitiesCalendarFeature.testState()
        )
    }
    Box(
        modifier = Modifier.systemBarsPadding()
    ) {
        CurrentUserAvailabilityView(
            state = state.value,
            listener = {
                state.value = AvailabilitiesCalendarFeature.reducer(it, state.value).first
            }
        )
    }
}