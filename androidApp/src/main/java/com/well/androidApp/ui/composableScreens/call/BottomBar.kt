package com.well.androidApp.ui.composableScreens.call

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.AmbientDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.πExt.Size
import com.well.androidApp.ui.composableScreens.πExt.heightPlusBottomSystemBars
import com.well.sharedMobile.ViewConstants.CallScreen.BottomBar.CallButtonOffset
import com.well.sharedMobile.ViewConstants.CallScreen.BottomBar.CallButtonPadding
import com.well.sharedMobile.ViewConstants.CallScreen.BottomBar.Height
import com.well.sharedMobile.ViewConstants.CallScreen.CallButtonRadius
import com.well.sharedMobile.puerh.call.CallFeature.Msg
import com.well.sharedMobile.puerh.call.CallFeature.State

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
                (CallButtonRadius + CallButtonPadding).dp,
                CallButtonOffset.dp,
                AmbientDensity.current
            )
        )
) {
    Image(
        vectorResource(R.drawable.ic_call_background),
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
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
                    listener(Msg.StartImageSharing)
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
            ToggleStillButton(
                R.drawable.ic_cam,
                !cameraEnabled,
                onClick = {
                    listener(toggleCameraMsg())
                }
            )
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

data class BottomShape(
    val radius: Dp,
    val offset: Dp,
    val density: Density,
) : Shape {
    private fun Dp.toPx() = with(density) { toPx() }

    override fun createOutline(
        size: Size,
        density: Density
    ) = Outline.Generic(Path().apply {
        fillType = PathFillType.EvenOdd
        addRect(Rect(Offset.Zero, size))
        val radius = radius.toPx()
        addArc(
            Rect(
                Offset(x = size.width / 2 - radius, y = -radius - offset.toPx()),
                Size(radius * 2)
            ),
            0F,
            180F,
        )
        close()
    })
}