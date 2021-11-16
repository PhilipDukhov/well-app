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
import com.well.modules.features.welcome.WelcomeFeature
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
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
    Welcome,
    Local,
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
        mutableStateOf(MyProfile)
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
                Welcome -> WelcomeScreen(WelcomeFeature.State()) {}
                Call -> CallTest()
                MyProfile -> MyProfileTest()
                Slider -> SliderTest()
                Local -> LocalTestScreen()
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
    val state = remember { mutableStateOf(0) }
    val titles = listOf("1", "2", "3", "4", "5")
    Column {
        ScrollableTabRow(
            selectedTabIndex = state.value,
            modifier = Modifier.wrapContentWidth(),
            edgePadding = 16.dp
        ) {
            titles.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = state.value == index,
                    onClick = { state.value = index }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = "${state.value + 1}",
            style = MaterialTheme.typography.body1
        )
    }
}