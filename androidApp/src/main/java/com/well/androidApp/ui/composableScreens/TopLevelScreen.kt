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
) = Crossfade(current = state.currentScreen) { screen ->
    when (screen) {
        is OnlineUsers -> OnlineUsersScreen(screen.state) {
            listener(TopLevelFeature.Msg.OnlineUsersMsg(it))
        }
        TopLevelFeature.State.ScreenState.Login -> Unit
    }
//    CallScreen(
//        User(
//            1,
//            "Phil",
//            "Dukhov aksdnkjansdkj ansjkd naksj dnkjas ndkja nskjd naksjdn akjsnd kjans dkja",
//            User.Type.Google,
//        )
//    )
}