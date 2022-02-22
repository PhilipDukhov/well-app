package com.well.androidAppTest

import com.well.modules.androidUi.composableScreens.calendar.CalendarScreen
import com.well.modules.features.calendar.calendarFeature.CalendarFeature
import com.well.sharedMobileTest.testState
import androidx.compose.runtime.Composable

@Composable
fun CalendarTest() {
    TestScreenReducerView(
        initial = CalendarFeature.State.testState,
        reducer = CalendarFeature::reducer,
        screen = { state, listener ->
            CalendarScreen(state, listener)
        }
    )
}