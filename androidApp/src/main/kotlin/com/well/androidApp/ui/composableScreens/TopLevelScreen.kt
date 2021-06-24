package com.well.androidApp.ui.composableScreens

import com.well.androidApp.ui.composableScreens.call.CallScreen
import com.well.androidApp.ui.composableScreens.experts.ExpertsScreen
import com.well.androidApp.ui.composableScreens.login.LoginScreen
import com.well.androidApp.ui.composableScreens.myProfile.MyProfileScreen
import com.well.sharedMobile.puerh._topLevel.ScreenState
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature
import com.well.sharedMobile.puerh.welcome.WelcomeFeature
import androidx.compose.runtime.Composable

@Composable
fun TopLevelScreenImpl(
    state: TopLevelFeature.State,
    listener: (TopLevelFeature.Msg) -> Unit,
) = state.currentScreen.let { screen ->
    when (screen) {
        is TopLevelFeature.State.Screen.Single -> {
            Screen(screen.screen, listener)
        }
        is TopLevelFeature.State.Screen.Tabs -> {
            screen.tabs.forEach { tabScreen ->
                Screen(tabScreen.screen, listener)
            }
        }
    }
}

@Composable
private fun Screen(screenState: ScreenState, listener: (TopLevelFeature.Msg) -> Unit) {
    when (screenState) {
        is ScreenState.Launch -> Unit
        is ScreenState.Welcome -> {
            listener(
                TopLevelFeature.Msg.WelcomeMsg(WelcomeFeature.Msg.Continue)
            )
        }
        is ScreenState.Login -> LoginScreen(screenState.state) {
            listener(TopLevelFeature.Msg.LoginMsg(it))
        }
        is ScreenState.Experts -> ExpertsScreen(screenState.state) {
            listener(TopLevelFeature.Msg.ExpertsMsg(it))
        }
        is ScreenState.Call -> {
            CallScreen(screenState.state) {
                listener(TopLevelFeature.Msg.CallMsg(it))
            }
        }
        is ScreenState.MyProfile -> MyProfileScreen(screenState.state) {
            listener(TopLevelFeature.Msg.MyProfileMsg(it))
        }
        is ScreenState.About -> TODO()
        is ScreenState.More -> TODO()
        is ScreenState.Support -> TODO()
    }
}