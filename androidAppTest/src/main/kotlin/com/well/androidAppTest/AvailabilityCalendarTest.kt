package com.well.androidAppTest

import com.well.modules.androidUi.composableScreens.myProfile.availability.AvailabilitiesCalendarView
import com.well.modules.features.myProfile.myProfileFeature.availabilitiesCalendar.AvailabilitiesCalendarFeature
import com.well.sharedMobileTest.testState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.google.accompanist.insets.systemBarsPadding

@Composable
fun AvailabilityCalendarTest() {
    val state = remember {
        mutableStateOf(
            AvailabilitiesCalendarFeature.testState(100)
        )
    }
    var padding by remember { mutableStateOf(0f) }
    Column(Modifier.systemBarsPadding()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = with(LocalDensity.current) { padding.toDp() })
        ) {
            key(padding) {
                AvailabilitiesCalendarView(
                    state = state.value,
                    listener = {
                        state.value = AvailabilitiesCalendarFeature.reducer(it, state.value).first
                    }
                )
            }
        }
        Slider(
            value = padding,
            onValueChange = { padding = it },
            valueRange = 0f..500f,
        )
    }
}