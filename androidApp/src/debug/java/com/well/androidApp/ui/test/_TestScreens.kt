package com.well.androidApp.ui.test

import androidx.compose.runtime.Composable
import com.well.androidApp.ui.test.TestScreen.*

@Suppress("RedundantNullableReturnType")
val testScreen: TestScreen? = MyProfile

enum class TestScreen {
    Call,
    MyProfile,
    Slider,
}

@Composable
fun TestComposeScreen(testScreen: TestScreen) {
    when (testScreen) {
        Call -> CallTest()
        MyProfile -> MyProfileTest()
        Slider -> SliderTest()
    }
}