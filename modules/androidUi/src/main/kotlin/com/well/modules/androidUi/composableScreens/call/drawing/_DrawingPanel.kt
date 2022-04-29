package com.well.modules.androidUi.composableScreens.call.drawing

import com.well.modules.androidUi.ext.toColor
import com.well.modules.features.call.callFeature.drawing.DrawingFeature.Msg
import com.well.modules.features.call.callFeature.drawing.DrawingFeature.State
import com.well.modules.models.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import coil.compose.rememberAsyncImagePainter

@Composable
fun DrawingPanel(
    state: State,
    modifier: Modifier,
    listener: (Msg) -> Unit,
) {
    Column(
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
                    Image(
                        rememberAsyncImagePainter(it.coilDataAny),
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
    }
}