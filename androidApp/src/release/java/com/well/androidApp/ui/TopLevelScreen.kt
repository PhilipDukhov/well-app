package com.well.androidApp.ui.composableScreens

import androidx.compose.runtime.Composable
import com.well.androidApp.ui.composableScreens.TopLevelScreenImpl

@Composable
fun TopLevelScreen(
    state: TopLevelFeature.State,
    listener: (TopLevelFeature.Msg) -> Unit,
) = TopLevelScreenImpl(state, listener)