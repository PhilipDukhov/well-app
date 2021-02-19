package com.well.androidApp.ui.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.well.androidApp.ui.composableScreens.myProfile.MyProfileScreen
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature

@Composable
internal fun MyProfileTest() {
    val state = remember {
        mutableStateOf(
            MyProfileFeature.testState()
        )
    }
    MyProfileScreen(
        state = state.value,
        listener = {
            println("msg $it")
            state.value = MyProfileFeature.reducer(it, state.value).first
        })
}