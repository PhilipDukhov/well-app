package com.well.androidApp.ui.composableScreens.call.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import com.well.androidApp.ui.composableScreens.πExt.toColor
import com.well.serverModels.Size
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature.Msg
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature.State
import com.google.accompanist.coil.CoilImage

@Composable
fun DrawingPanel(
    state: State,
    modifier: Modifier,
    listener: (Msg) -> Unit,
) = Column(
    modifier = modifier
        .fillMaxSize()
) {
    TopPanel(
        state = state,
        listener = listener,
        modifier = Modifier
            .fillMaxWidth()
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1F)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    listener(Msg.UpdateLocalImageContainerSize(Size(it.width, it.height)))
                })
        {
            state.image?.let {
                CoilImage(
                    it.coilDataAny,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                )
                DrawingContent(
                    state, listener,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            val lineWidth = remember { mutableStateOf(state.lineWidth) }
            WidthSlider(
                lineWidth,
                previewThumbColor = state.currentColor.toColor(),
                thumbRadiusRange = State.lineWidthRange,
                onValueChangeEnd = {
                    listener(Msg.UpdateLineWidth(lineWidth.value))
                },
                modifier = Modifier
                    .weight(4f)
                    .fillMaxWidth(0.5F)
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
    BottomPanel(
        state = state,
        listener = listener,
    )
} // Column
