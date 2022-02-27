package com.well.modules.androidUi.composableScreens.call

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import com.well.modules.androidUi.ext.TextKMM
import com.well.modules.androidUi.helpers.NextSecondNotifier
import com.well.modules.models.Color
import com.well.modules.features.call.callFeature.CallFeature

@Composable
fun SecondsPassedText(
    info: CallFeature.State.CallStartedDateInfo?
) {
    if (info == null) return
    val secondsPassed = remember { mutableStateOf(info.secondsPassedFormatted) }
    val scope = rememberCoroutineScope()
    val nextSecondNotifier = remember {
        NextSecondNotifier(scope) {
            secondsPassed.value = info.secondsPassedFormatted
        }
    }
    nextSecondNotifier.date = info.date.date
    DisposableEffect(Unit) {
        onDispose {
            nextSecondNotifier.close()
        }
    }
    TextKMM(
        text = secondsPassed.value,
        color = Color.White,
        style = MaterialTheme.typography.body1,
    )
}