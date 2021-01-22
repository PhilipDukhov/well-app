package com.well.androidApp.ui.composableScreens.call

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.onDispose
import androidx.compose.runtime.remember
import com.well.androidApp.ui.composableScreens.πExt.TextW
import com.well.androidApp.ui.helpers.NextSecondNotifier
import com.well.serverModels.Color
import com.well.sharedMobile.puerh.call.CallFeature

@Composable
fun SecondsPassedText(
    info: CallFeature.State.CallStartedDateInfo?
) {
    if (info == null) return
    val secondsPassed = remember { mutableStateOf(info.secondsPassedFormatted) }
    val nextSecondNotifier = remember {
        NextSecondNotifier {
            secondsPassed.value = info.secondsPassedFormatted
        }
    }
    nextSecondNotifier.date = info.date.date
    onDispose {
        nextSecondNotifier.close()
    }
    TextW(
        text = secondsPassed.value,
        color = Color.White,
        style = MaterialTheme.typography.body1,
    )
}