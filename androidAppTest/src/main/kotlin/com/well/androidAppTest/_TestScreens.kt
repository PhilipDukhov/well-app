package com.well.androidAppTest

import com.well.modules.androidUi.composableScreens.call.CallScreen
import com.well.modules.androidUi.composableScreens.more.FavoritesScreen
import com.well.modules.androidUi.composableScreens.userChat.UserChatScreen
import com.well.modules.androidUi.composableScreens.welcome.WelcomeScreen
import com.well.modules.androidUi.customViews.ProfileImage
import com.well.modules.features.call.callFeature.CallFeature
import com.well.modules.features.more.moreFeature.subfeatures.FavoritesFeature
import com.well.modules.features.userChat.userChatFeature.UserChatFeature
import com.well.modules.features.welcome.WelcomeFeature
import com.well.modules.models.User
import com.well.modules.models.chat.ChatMessage
import com.well.modules.models.chat.ChatMessageViewModel
import com.well.modules.utils.viewUtils.sharedImage.UrlImage
import com.well.sharedMobileTest.testState
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.statusBarsPadding

enum class TestScreen {
    Local,

    Welcome,
    Call,
    MyProfile,
    Slider,
    UserChat,
    UserRating,
    AvailabilityCalendar,
    Calendar,
    GradientView,
    DoubleCalendar,
    Favorites,
}

private var selectedScreen: TestScreen by mutableStateOf(TestScreen.Favorites)
private var opened by mutableStateOf(true)

@Composable
fun TestComposeScreen() {
    if (opened) {
        BackHandler {
            opened = false
        }
        Column {
            when (selectedScreen) {
                TestScreen.Local -> LocalTestScreen()
                TestScreen.Welcome -> WelcomeScreen(WelcomeFeature.State()) {}
                TestScreen.MyProfile -> MyProfileTest()
                TestScreen.Slider -> SliderTest()
                TestScreen.UserRating -> UserRatingTest()
                TestScreen.AvailabilityCalendar -> AvailabilityCalendarTest()
                TestScreen.Calendar -> CalendarTest()
                TestScreen.GradientView -> GradientViewTest()
                TestScreen.DoubleCalendar -> {
                    Box(Modifier.weight(1f)) {
                        CalendarTest()
                    }
                    Box(Modifier.weight(1f)) {
                        AvailabilityCalendarTest()
                    }
                }
                TestScreen.Call -> {
                    TestScreenReducerView(
                        initial = CallFeature.testState(CallFeature.State.Status.Ongoing),
                        reducer = CallFeature::reducer,
                        screen = { state, listener ->
                            CallScreen(state, listener)
                        },
                    )
                }
                TestScreen.UserChat -> {
                    TestScreenReducerView(
                        initial = UserChatFeature.State(
                            peerId = User.Id(0), user = null, backToUser = false,
                            messages = List(100) {
                                ChatMessageViewModel(
                                    id = ChatMessage.Id(it.toLong()),
                                    creation = 0.0,
                                    content = ChatMessage.Content.Text("akjsdnasjk"),
                                    status = ChatMessageViewModel.Status.IncomingRead,
                                )
                            }
                        ),
                        reducer = UserChatFeature::reducer,
                        screen = { state, listener ->
                            UserChatScreen(state, listener)
                        },
                    )
                }
                TestScreen.Favorites -> {
                    TestScreenReducerView(
                        FavoritesFeature.testState(),
                        FavoritesFeature::reducer,
                        screen = { state, listener ->
                            FavoritesScreen(state, listener)
                        },
                    )
                }
            }
        }
    } else {
        SelectionScreen()
    }
}

@Composable
fun SelectionScreen() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ScrollableTabRow(
            selectedTabIndex = TestScreen.values().indexOf(selectedScreen),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TestScreen.values().forEach { screen ->
                Tab(selected = screen == selectedScreen, onClick = {
                    selectedScreen = screen
                    opened = true
                }) {
                    Text(
                        screen.name,
                        color = Color.White,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        }
        Button(onClick = { opened = true }) {
            Text("Open")
        }
    }
}

@Composable
fun LocalTestScreen() {
    ProfileImage(
        UrlImage("https://s3.us-east-2.amazonaws.com/well-images/profilePictures/3-932c9f30-d066-43ac-beef-a558ea9d07fa..jpeg"),
        shape = RectangleShape,
        aspectRatio = 1.2f,
        modifier = Modifier
            .fillMaxWidth()
    )
}