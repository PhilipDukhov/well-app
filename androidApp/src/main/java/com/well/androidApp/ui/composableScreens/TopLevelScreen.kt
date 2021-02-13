package com.well.androidApp.ui.composableScreens

import androidx.compose.runtime.Composable
import com.well.androidApp.ui.composableScreens.call.CallScreen
import com.well.androidApp.ui.composableScreens.onlineUsers.OnlineUsersScreen
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature.State.ScreenState.OnlineUsers

@Composable
fun TopLevelScreenImpl(
    state: TopLevelFeature.State,
    listener: (TopLevelFeature.Msg) -> Unit,
) = state.currentScreen.let { screen ->
    println("current screen $screen")
    when (screen) {
        is TopLevelFeature.State.ScreenState.Login -> Unit
        is OnlineUsers -> OnlineUsersScreen(screen.state) {
            listener(TopLevelFeature.Msg.OnlineUsersMsg(it))
        }
        is TopLevelFeature.State.ScreenState.Call -> {
            CallScreen(screen.state) {
                listener(TopLevelFeature.Msg.CallMsg(it))
            }
        }
    }
}