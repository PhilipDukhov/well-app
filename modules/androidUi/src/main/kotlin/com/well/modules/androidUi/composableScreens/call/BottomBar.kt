package com.well.modules.androidUi.composableScreens.call

import com.well.modules.androidUi.R
import com.well.modules.androidUi.customViews.GradientView
import com.well.modules.androidUi.ext.Size
import com.well.modules.androidUi.ext.heightPlusBottomSystemBars
import com.well.modules.androidUi.ext.toPx
import com.well.modules.features.call.callFeature.CallFeature.Msg
import com.well.modules.features.call.callFeature.CallFeature.State
import com.well.modules.utils.viewUtils.Gradient
import com.well.modules.utils.viewUtils.ViewConstants.CallScreen.BottomBar.CallButtonOffset
import com.well.modules.utils.viewUtils.ViewConstants.CallScreen.BottomBar.CallButtonPadding
import com.well.modules.utils.viewUtils.ViewConstants.CallScreen.BottomBar.Height
import com.well.modules.utils.viewUtils.ViewConstants.CallScreen.CallButtonRadius
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun BottomBar(
    state: State,
    listener: (Msg) -> Unit,
) = Box(
    modifier = Modifier
        .fillMaxWidth()
        .heightPlusBottomSystemBars(Height.dp)
        .clip(
            BottomShape(
                radius = (CallButtonRadius + CallButtonPadding).dp,
                offset = CallButtonOffset.dp,
            )
        )
) {
    GradientView(
        gradient = Gradient.CallBottomBar,
        modifier = Modifier
            .fillMaxSize()
    )
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(Height.dp)
            .fillMaxWidth()
    ) {
        state.localDeviceState.apply {
            ToggleStillButton(
                R.drawable.ic_screen_sharing,
                false,
                onClick = {
                    listener(Msg.InitializeDrawing)
                }
            )
            ToggleStillButton(
                R.drawable.ic_mic,
                !micEnabled,
                onClick = {
                    listener(toggleMicMsg())
                }
            )
            SecondsPassedText(state.callStartedDateInfo)
            if (state.viewPoint == State.ViewPoint.Both) {
                ToggleStillButton(
                    R.drawable.ic_cam,
                    !cameraEnabled,
                    onClick = {
                        listener(toggleCameraMsg())
                    }
                )
            } else {
                ToggleStillButton(
                    R.drawable.ic_stop,
                    false,
                    onClick = {
                        listener(Msg.LocalUpdateViewPoint(State.ViewPoint.Both))
                    }
                )
            }
            ToggleStillButton(
                R.drawable.ic_audio,
                audioSpeakerEnabled,
                onClick = {
                    listener(toggleAudioSpeakerMsg())
                }
            )
        }
    } // Row
} // Box

private data class BottomShape(
    val radius: Dp,
    val offset: Dp,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) = Outline.Generic(Path().apply {
        fillType = PathFillType.EvenOdd
        addRect(Rect(Offset.Zero, size))
        val radius = radius.toPx(density)
        addArc(
            Rect(
                Offset(x = size.width / 2 - radius, y = -radius - offset.toPx(density)),
                Size(radius * 2)
            ),
            0F,
            180F,
        )
        close()
    })
}