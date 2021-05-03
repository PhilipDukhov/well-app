package com.well.androidApp.ui.composableScreens.welcome

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.πCustomViews.Control
import com.well.androidApp.ui.composableScreens.πCustomViews.GradientView
import com.well.androidApp.ui.composableScreens.πCustomViews.InactiveOverlay
import com.well.sharedMobile.puerh.login.SocialNetwork
import com.well.sharedMobile.utils.Gradient
import com.google.accompanist.insets.systemBarsPadding
import com.well.sharedMobile.puerh.welcome.WelcomeFeature.State
import com.well.sharedMobile.puerh.welcome.WelcomeFeature.Msg

import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

@Composable
fun LoginScreen(
    state: State,
    listener: (Msg) -> Unit,
) = Box(contentAlignment = Alignment.BottomCenter) {
//    state.title
    val isSelected = false
    val backgroundColor by animateColorAsState(if (isSelected) Color.Red else Color.Transparent)

}