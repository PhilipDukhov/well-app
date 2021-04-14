package com.well.androidApp.ui.composableScreens

import androidx.compose.runtime.Composable
import com.well.androidApp.ui.composableScreens.call.CallScreen
import com.well.androidApp.ui.composableScreens.login.LoginScreen
import com.well.androidApp.ui.composableScreens.myProfile.MyProfileScreen
import com.well.androidApp.ui.composableScreens.onlineUsers.OnlineUsersScreen
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature
import com.well.sharedMobile.puerh._topLevel.ScreenState

@Composable
fun TopLevelScreenImpl(
    state: TopLevelFeature.State,
    listener: (TopLevelFeature.Msg) -> Unit,
) = state.currentScreen.let { screen ->
    when (screen) {
        is ScreenState.Launch -> Unit
        is ScreenState.Login -> LoginScreen(screen.state) {
            listener(TopLevelFeature.Msg.LoginMsg(it))
        }
        is ScreenState.OnlineUsers -> OnlineUsersScreen(screen.state) {
            listener(TopLevelFeature.Msg.OnlineUsersMsg(it))
        }
        is ScreenState.Call -> {
            CallScreen(screen.state) {
                listener(TopLevelFeature.Msg.CallMsg(it))
            }
        }
        is ScreenState.MyProfile -> MyProfileScreen(screen.state) {
            listener(TopLevelFeature.Msg.MyProfileMsg(it))
        }
    }
}