package com.well.modules.androidUi.composableScreens

import com.well.modules.androidUi.R
import com.well.modules.androidUi.composableScreens.calendar.CalendarScreen
import com.well.modules.androidUi.composableScreens.call.CallScreen
import com.well.modules.androidUi.composableScreens.chatList.ChatListScreen
import com.well.modules.androidUi.composableScreens.experts.ExpertsScreen
import com.well.modules.androidUi.composableScreens.login.LoginScreen
import com.well.modules.androidUi.composableScreens.more.AboutScreen
import com.well.modules.androidUi.composableScreens.more.ActivityHistoryScreen
import com.well.modules.androidUi.composableScreens.more.DonateScreen
import com.well.modules.androidUi.composableScreens.more.FavoritesScreen
import com.well.modules.androidUi.composableScreens.more.MoreScreen
import com.well.modules.androidUi.composableScreens.more.SupportScreen
import com.well.modules.androidUi.composableScreens.more.WellAcademyScreen
import com.well.modules.androidUi.composableScreens.myProfile.MyProfileScreen
import com.well.modules.androidUi.composableScreens.updateRequest.UpdateRequestScreen
import com.well.modules.androidUi.composableScreens.userChat.UserChatScreen
import com.well.modules.androidUi.composableScreens.welcome.WelcomeScreen
import com.well.modules.androidUi.customViews.AutoSizeText
import com.well.modules.androidUi.ext.toColor
import com.well.modules.features.topLevel.topLevelFeature.ScreenState
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature.State.Tab
import com.well.modules.models.Color
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature as Feature
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import com.google.accompanist.insets.navigationBarsPadding

@Composable
fun TopLevelScreen(
    state: Feature.State,
    listener: (Feature.Msg) -> Unit,
) {
    val screen = state.currentScreen
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
                    backgroundColor = Color.White.toColor(),
                    modifier = Modifier
                        .navigationBarsPadding()
                ) {
                    screen.tabs.forEach { tabScreen ->
                        val unreadCount =
                            (tabScreen.screen as? ScreenState.ChatList)?.state?.unreadCount
                                ?: (tabScreen.screen as? ScreenState.Calendar)?.state?.unreadCount
                                ?: 0
                        BottomNavigationItem(
                            selected = state.selectedTab == tabScreen.tab,
                            onClick = {
                                listener(Feature.Msg.SelectTab(tabScreen.tab))
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (unreadCount > 0) {
                                            Badge {
                                                Text("$unreadCount")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        painter = tabScreen.tab.iconPainter(),
                                        contentDescription = null,
                                    )
                                }
                            },
                            label = {
                                AutoSizeText(
                                    text = tabScreen.tab.spacedName(),
                                )
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
        Tab.Calendar -> {
            rememberVectorPainter(Icons.Rounded.CalendarToday)
        }
        else -> {
            error("unexpected tab icon request: $this")
        }
    }

@Composable
private fun ColumnScope.Screen(screenState: ScreenState, listener: (Feature.Msg) -> Unit) {
    when (screenState) {
        is ScreenState.Launch -> Unit
        is ScreenState.Welcome -> WelcomeScreen(screenState.state) {
            listener(screenState.mapMsgToTopLevel(it))
        }
        is ScreenState.Login -> LoginScreen(screenState.state) {
            listener(screenState.mapMsgToTopLevel(it))
        }
        is ScreenState.Experts -> ExpertsScreen(screenState.state) {
            listener(screenState.mapMsgToTopLevel(it))
        }
        is ScreenState.Call -> CallScreen(screenState.state) {
            listener(screenState.mapMsgToTopLevel(it))
        }
        is ScreenState.MyProfile -> MyProfileScreen(screenState.state) {
            listener(screenState.mapMsgToTopLevel(it))
        }
        is ScreenState.More -> MoreScreen(screenState.state) {
            listener(screenState.mapMsgToTopLevel(it))
        }
        is ScreenState.About -> AboutScreen(screenState.state) {
            listener(screenState.mapMsgToTopLevel(it))
        }
        is ScreenState.Support -> SupportScreen(screenState.state) {
            listener(screenState.mapMsgToTopLevel(it))
        }
        is ScreenState.WellAcademy -> WellAcademyScreen(screenState.state) {
            listener(screenState.mapMsgToTopLevel(it))
        }
        is ScreenState.ChatList -> ChatListScreen(screenState.state) {
            listener(screenState.mapMsgToTopLevel(it))
        }
        is ScreenState.UserChat -> UserChatScreen(screenState.state) {
            listener(screenState.mapMsgToTopLevel(it))
        }
        is ScreenState.Calendar -> CalendarScreen(screenState.state) {
            listener(screenState.mapMsgToTopLevel(it))
        }
        is ScreenState.UpdateRequest -> UpdateRequestScreen(screenState.state) {
            listener(screenState.mapMsgToTopLevel(it))
        }
        is ScreenState.ActivityHistory -> ActivityHistoryScreen(screenState.state) {
            listener(screenState.mapMsgToTopLevel(it))
        }
        is ScreenState.Donate -> DonateScreen(screenState.state) {
            listener(screenState.mapMsgToTopLevel(it))
        }
        is ScreenState.Favorites -> FavoritesScreen(screenState.state) {
            listener(screenState.mapMsgToTopLevel(it))
        }
    }
}