package com.well.androidAppTest

import com.well.androidAppTest.TestScreen.AvailabilityCalendar
import com.well.androidAppTest.TestScreen.Call
import com.well.androidAppTest.TestScreen.Local
import com.well.androidAppTest.TestScreen.MyProfile
import com.well.androidAppTest.TestScreen.Slider
import com.well.androidAppTest.TestScreen.UserChat
import com.well.androidAppTest.TestScreen.UserRating
import com.well.androidAppTest.TestScreen.Welcome
import com.well.androidAppTest.TestScreen.values
import com.well.modules.androidUi.composableScreens.welcome.WelcomeScreen
import com.well.modules.androidUi.customViews.ProfileImage
import com.well.modules.features.welcome.WelcomeFeature
import com.well.modules.utils.viewUtils.sharedImage.UrlImage
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
}

@Composable
fun TestComposeScreen() {
    var selectedScreen: TestScreen by rememberSaveable(Unit) {
        mutableStateOf(Local)
    }
    var opened by remember {
        mutableStateOf(true)
    }
    if (opened) {
        BackHandler {
            opened = false
        }
        Column {
            when (selectedScreen) {
                Local -> LocalTestScreen()
                Welcome -> WelcomeScreen(WelcomeFeature.State()) {}
                Call -> CallTest()
                MyProfile -> MyProfileTest()
                Slider -> SliderTest()
                UserChat -> UserChatTest()
                UserRating -> UserRatingTest()
                AvailabilityCalendar -> AvailabilityCalendarTest()
            }
        }
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
                .statusBarsPadding()
        ) {
            ScrollableTabRow(
                selectedTabIndex = values().indexOf(selectedScreen),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                values().forEach { screen ->
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
}

@Composable
fun LocalTestScreen() {
    ProfileImage(
        UrlImage("https://s3.us-east-2.amazonaws.com/well-images/profilePictures/3-932c9f30-d066-43ac-beef-a558ea9d07fa..jpeg"),
        squareCircleShaped = false,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
    )
}