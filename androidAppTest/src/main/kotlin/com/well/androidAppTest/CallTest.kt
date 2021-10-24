package com.well.androidAppTest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.well.modules.androidUi.composableScreens.call.CallScreen
import com.well.modules.features.call.callFeature.CallFeature
import com.well.sharedMobileTest.testState

@Composable
internal fun CallTest() {
    val state = remember {
        mutableStateOf(
            CallFeature.testState(CallFeature.State.Status.Ongoing)
        )
    }
    CallScreen(
        state = state.value,
        listener = {
            state.value = CallFeature.reducer(it, state.value).first
        })
}