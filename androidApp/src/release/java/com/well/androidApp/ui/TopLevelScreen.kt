package com.well.androidApp.ui

import androidx.compose.runtime.Composable
import com.well.androidApp.ui.composableScreens.TopLevelScreenImpl
import com.well.sharedMobile.TopLevelFeature

@Composable
fun TopLevelScreen(
    state: TopLevelFeature.State,
    listener: (TopLevelFeature.Msg) -> Unit,
) = TopLevelScreenImpl(state, listener)