package com.well.androidApp.ui.test

import com.well.androidApp.ui.composableScreens.myProfile.CurrentUserAvailabilityView
import com.well.sharedMobile.puerh.myProfile.currentUserAvailability.CurrentUserAvailabilityFeature
import com.well.sharedMobile.testData.testState
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
            CurrentUserAvailabilityFeature.testState()
        )
    }
    Box(
        modifier = Modifier.systemBarsPadding()
    ) {
        CurrentUserAvailabilityView(
            state = state.value,
            listener = {
                state.value = CurrentUserAvailabilityFeature.reducer(it, state.value).first
            }
        )
    }
}