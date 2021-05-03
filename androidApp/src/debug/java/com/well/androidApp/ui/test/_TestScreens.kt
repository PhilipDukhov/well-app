package com.well.androidApp.ui.test

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.well.androidApp.ui.test.TestScreen.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment

@Suppress("RedundantNullableReturnType")
val testScreen: TestScreen? = null

enum class TestScreen {
    Local,
    Call,
    MyProfile,
    Slider,
}

@Composable
fun TestComposeScreen(testScreen: TestScreen) {
    when (testScreen) {
        Call -> CallTest()
        MyProfile -> MyProfileTest()
        Slider -> SliderTest()
        Local -> LocalTestScreen()
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
