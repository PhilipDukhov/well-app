package com.well.androidApp.ui.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.well.androidApp.ui.composableScreens.call.drawing.WidthSlider
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature

@Composable
fun SliderTest() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Blue)

    ) {
        val state = mutableStateOf(5f)
        WidthSlider(
            state,
            previewThumbColor = Color.Red,
            thumbRadiusRange = DrawingFeature.State.lineWidthRange,
            onValueChangeEnd = {

            },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(0.5f)
                .fillMaxHeight(0.5f)
        )
    }
}