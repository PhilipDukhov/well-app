package com.well.androidApp.ui.composableScreens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.well.androidApp.ui.composableScreens.call.CallScreen
import com.well.androidApp.ui.composableScreens.login.LoginScreen
import com.well.androidApp.ui.composableScreens.myProfile.MyProfileScreen
import com.well.androidApp.ui.composableScreens.onlineUsers.OnlineUsersScreen
import com.well.androidApp.ui.composableScreens.πExt.widthDp
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature.State.ScreenState.OnlineUsers

@Composable
fun TopLevelScreenImpl(
    state: TopLevelFeature.State,
    listener: (TopLevelFeature.Msg) -> Unit,
) = state.currentScreen.let { screen ->
    when (screen) {
        is TopLevelFeature.State.ScreenState.LaunchScreen -> Unit
        is TopLevelFeature.State.ScreenState.Login -> LoginScreen(screen.state) {
            listener(TopLevelFeature.Msg.LoginMsg(it))
        }
        is OnlineUsers -> OnlineUsersScreen(screen.state) {
            listener(TopLevelFeature.Msg.OnlineUsersMsg(it))
        }
        is TopLevelFeature.State.ScreenState.Call -> {
            CallScreen(screen.state) {
                listener(TopLevelFeature.Msg.CallMsg(it))
            }
        }
        is TopLevelFeature.State.ScreenState.MyProfile -> MyProfileScreen(screen.state) {
            listener(TopLevelFeature.Msg.MyProfileMsg(it))
        }
    }
}