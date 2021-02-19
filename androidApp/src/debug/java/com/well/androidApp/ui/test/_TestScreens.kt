package com.well.androidApp.ui.test

import androidx.compose.runtime.Composable
import com.well.androidApp.ui.test.TestScreen.*

@Suppress("RedundantNullableReturnType")
val testScreen: TestScreen? = MyProfile

enum class TestScreen {
    ImageSharing,
    Call,
    MyProfile,
}

@Composable
fun TestComposeScreen(testScreen: TestScreen) {
    when (testScreen) {
        ImageSharing -> ImageSharingTest()
        Call -> CallTest()
        MyProfile -> MyProfileTest()
    }
}