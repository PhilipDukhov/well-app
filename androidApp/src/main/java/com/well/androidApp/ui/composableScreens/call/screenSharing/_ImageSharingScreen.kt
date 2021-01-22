package com.well.androidApp.ui.composableScreens.call.screenSharing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.WithConstraints
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.well.androidApp.ui.composableScreens.πExt.toColor
import com.well.serverModels.Size
import com.well.sharedMobile.puerh.call.imageSharing.ImageSharingFeature.Msg
import com.well.sharedMobile.puerh.call.imageSharing.ImageSharingFeature.State
import dev.chrisbanes.accompanist.coil.CoilImage

@Composable
fun ImageSharingScreen(
    state: State,
    listener: (Msg) -> Unit,
) = Column(
    modifier = Modifier
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
        WithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    listener(Msg.UpdateLocalViewSize(Size(it.width, it.height)))
                }
        ) {
            state.image?.let {
                CoilImage(
                    it.data,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
        DrawingContent(
            state, listener,
            modifier = Modifier.fillMaxSize()
        )
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
