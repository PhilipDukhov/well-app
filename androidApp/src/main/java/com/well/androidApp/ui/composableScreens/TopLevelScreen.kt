package com.well.androidApp.ui.composableScreens

import androidx.compose.runtime.Composable
import com.well.androidApp.ui.composableScreens.call.CallScreen
import com.well.androidApp.ui.composableScreens.call.screenSharing.ImageSharingScreen
import com.well.androidApp.ui.composableScreens.onlineUsers.OnlineUsersScreen
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature.State.Screen.OnlineUsers

@Composable
fun TopLevelScreen(
    state: TopLevelFeature.State,
    listener: (TopLevelFeature.Msg) -> Unit,
) = state.currentScreen.let { screen ->
    println("current screen $screen")
    when (screen) {
        is OnlineUsers -> OnlineUsersScreen(screen.state) {
            listener(TopLevelFeature.Msg.OnlineUsersMsg(it))
        }
        is TopLevelFeature.State.Screen.Login -> Unit
        is TopLevelFeature.State.Screen.Call -> {
            CallScreen(screen.state) {
                listener(TopLevelFeature.Msg.CallMsg(it))
            }
        }
        is TopLevelFeature.State.Screen.ImageSharing -> {
            ImageSharingScreen(screen.state) {
                listener(TopLevelFeature.Msg.ImageSharingMsg(it))
            }
        }
    }
}