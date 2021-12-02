package com.well.androidAppTest

import com.well.modules.androidUi.composableScreens.calendar.CalendarScreen
import com.well.modules.features.calendar.calendarFeature.CalendarFeature
import com.well.sharedMobileTest.testState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun CalendarTest() {
    var state by remember { mutableStateOf(CalendarFeature.State.testState) }
    CalendarScreen(
        state = state,
        listener = {
            state = CalendarFeature.reducer(it, state).first
        }
    )
}