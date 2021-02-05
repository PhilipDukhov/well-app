package com.well.androidApp.ui

import androidx.compose.runtime.Composable
import com.well.androidApp.ui.composableScreens.TopLevelScreenImpl
import com.well.androidApp.ui.test.TestComposeScreen
import com.well.androidApp.ui.test.testScreen
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature

@Composable
fun TopLevelScreen(
    state: TopLevelFeature.State,
    listener: (TopLevelFeature.Msg) -> Unit,
) = if (testScreen != null) TestComposeScreen(testScreen) else TopLevelScreenImpl(state, listener)