package com.well.androidAppTest

import com.well.modules.androidUi.composableScreens.call.CallScreen
import com.well.modules.features.call.callFeature.CallFeature
import com.well.sharedMobileTest.testState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
internal fun CallTest() {
    var state by remember {
        mutableStateOf(
            CallFeature.testState(CallFeature.State.Status.Ongoing)
        )
    }
    CallScreen(
        state = state,
        listener = {
            state = CallFeature.reducer(it, state).first
        }
    )
}