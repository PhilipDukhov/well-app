package com.well.androidApp.ui.test

import androidx.compose.runtime.Composable
import com.well.androidApp.ui.test.TestScreen.*

@Suppress("RedundantNullableReturnType")
val testScreen: TestScreen? = null//ImageSharing

enum class TestScreen {
    ImageSharing,
    Call,
}

@Composable
fun TestComposeScreen(testScreen: TestScreen) {
    when (testScreen) {
        ImageSharing -> ImageSharingTest()
        Call -> CallTest()
    }
}