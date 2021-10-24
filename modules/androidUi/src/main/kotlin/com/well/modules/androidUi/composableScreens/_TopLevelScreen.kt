package com.well.modules.androidUi.composableScreens

import com.well.modules.androidUi.R
import com.well.modules.androidUi.composableScreens.call.CallScreen
import com.well.modules.androidUi.composableScreens.chatList.ChatListScreen
import com.well.modules.androidUi.composableScreens.experts.ExpertsScreen
import com.well.modules.androidUi.composableScreens.login.LoginScreen
import com.well.modules.androidUi.composableScreens.more.AboutScreen
import com.well.modules.androidUi.composableScreens.more.MoreScreen
import com.well.modules.androidUi.composableScreens.more.SupportScreen
import com.well.modules.androidUi.composableScreens.myProfile.MyProfileScreen
import com.well.modules.androidUi.composableScreens.userChat.UserChatScreen
import com.well.modules.androidUi.composableScreens.welcome.WelcomeScreen
import com.well.modules.androidUi.ext.toColor
import com.well.modules.models.Color
import com.well.sharedMobile.ScreenState
import com.well.sharedMobile.TopLevelFeature.State.Tab
import com.well.sharedMobile.TopLevelFeature as Feature
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import com.google.accompanist.insets.navigationBarsPadding

@Composable
fun TopLevelScreen(
    state: Feature.State,
    listener: (Feature.Msg) -> Unit,
) = state.currentScreen.let { screen ->
    Column(
        Modifier.fillMaxSize()
    ) {
        when (screen) {
            is Feature.State.Screen.Single -> {
                Screen(screen.screen, listener)
            }
            is Feature.State.Screen.Tabs -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    Screen(screen[state.selectedTab].screen, listener)
                }
                BottomNavigation(
                    modifier = Modifier
                        .navigationBarsPadding(),
                    backgroundColor = Color.White.toColor(),
                ) {
                    screen.tabs.forEach { tabScreen ->
                        BottomNavigationItem(
                            selected = state.selectedTab == tabScreen.tab,
                            onClick = {
                                listener(Feature.Msg.SelectTab(tabScreen.tab))
                            },
                            icon = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        painter = tabScreen.tab.iconPainter(),
                                        contentDescription = null,
                                        tint = LocalContentColor.current,
                                    )
                                    Text(
                                        text = tabScreen.tab.spacedName(),
                                        color = LocalContentColor.current,
                                        style = MaterialTheme.typography.caption,
                                    )
                                }
                            },
                            selectedContentColor = Color.DarkBlue.toColor(),
                            unselectedContentColor = Color.LightGray.toColor(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Tab.iconPainter() =
    when (this) {
        Tab.MyProfile -> {
            rememberVectorPainter(Icons.Default.Person)
        }
        Tab.Experts -> {
            painterResource(R.drawable.ic_expert_tab)
        }
        Tab.ChatList -> {
            rememberVectorPainter(Icons.Rounded.ChatBubble)
        }
        Tab.More -> {
            rememberVectorPainter(Icons.Rounded.Menu)
        }
        else -> {
            error("unexpected tab icond request: $this")
        }
    }

@Composable
private fun ColumnScope.Screen(screenState: ScreenState, listener: (Feature.Msg) -> Unit) {
    when (screenState) {
        is ScreenState.Launch -> Unit
        is ScreenState.Welcome -> WelcomeScreen(screenState.state) {
            listener(Feature.Msg.WelcomeMsg(it))
        }
        is ScreenState.Login -> LoginScreen(screenState.state) {
            listener(Feature.Msg.LoginMsg(it))
        }
        is ScreenState.Experts -> ExpertsScreen(screenState.state) {
            listener(Feature.Msg.ExpertsMsg(it))
        }
        is ScreenState.Call -> CallScreen(screenState.state) {
            listener(Feature.Msg.CallMsg(it))
        }
        is ScreenState.MyProfile -> MyProfileScreen(screenState.state) {
            listener(Feature.Msg.MyProfileMsg(it))
        }
        is ScreenState.More -> MoreScreen(screenState.state) {
            listener(Feature.Msg.MoreMsg(it))
        }
        is ScreenState.About -> AboutScreen(screenState.state) {
            listener(Feature.Msg.AboutMsg(it))
        }
        is ScreenState.Support -> SupportScreen(screenState.state) {
            listener(Feature.Msg.SupportMsg(it))
        }
        is ScreenState.ChatList -> ChatListScreen(screenState.state) {
            listener(Feature.Msg.ChatListMsg(it))
        }
        is ScreenState.UserChat -> UserChatScreen(screenState.state) {
            listener(Feature.Msg.UserChatMsg(it))
        }
    }
}