package com.well.androidApp.ui.composableScreens

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import com.well.androidApp.ui.composableScreens.call.CallScreen
import com.well.serverModels.User
import com.well.shared.puerh.topLevel.TopLevelFeature
import com.well.shared.puerh.topLevel.TopLevelFeature.State.ScreenState.OnlineUsers

@Composable
fun TopLevelScreen(
    state: TopLevelFeature.State,
    listener: (TopLevelFeature.Msg) -> Unit
) = //Crossfade(current = state.currentScreen) { screen ->
    state.currentScreen.let { screen ->

        println("current screen $screen")
        when (screen) {
            is OnlineUsers -> OnlineUsersScreen(screen.state) {
                listener(TopLevelFeature.Msg.OnlineUsersMsg(it))
            }
            is TopLevelFeature.State.ScreenState.Login -> Unit
            is TopLevelFeature.State.ScreenState.Call -> {
                CallScreen(screen.state) {
                    listener(TopLevelFeature.Msg.CallMsg(it))
                }
            }
        }
    }