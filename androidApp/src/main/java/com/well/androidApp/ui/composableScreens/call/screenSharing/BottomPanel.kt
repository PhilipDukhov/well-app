package com.well.androidApp.ui.composableScreens.call.screenSharing

import androidx.compose.foundation.InteractionState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.call.ToggleStillButton
import com.well.androidApp.ui.composableScreens.πExt.backgroundW
import com.well.androidApp.ui.composableScreens.πExt.borderW
import com.well.serverModels.Color
import com.well.sharedMobile.puerh.call.imageSharing.ImageSharingFeature.Msg
import com.well.sharedMobile.puerh.call.imageSharing.ImageSharingFeature.State
import dev.chrisbanes.accompanist.insets.navigationBarsPadding

@Composable
fun BottomPanel(
    state: State,
    listener: (Msg) -> Unit,
    modifier: Modifier = Modifier,
) = Column(
    modifier = modifier
        .backgroundW(Color.MineShaft)
        .navigationBarsPadding()
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
     ) {
        State.drawingColors
            .forEach { color ->
                val selected = state.currentColor == color
                Box(
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                listener(Msg.UpdateColor(color))
                            },
                            interactionState = remember { InteractionState() },
                            indication = rememberRipple(
                                bounded = false,
                                radius = 20.dp,
                            )
                        )
                        .weight(1f)
                        .aspectRatio(1F)
                        .padding(10.dp)
                        .borderW(
                            width = 2.dp,
                            color = if (selected) Color.AzureRadiance else Color.Transparent,
                            shape = CircleShape,
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .padding(3.dp)
                            .clip(CircleShape)
                            .backgroundW(color)
                            .fillMaxSize()
                    )
                }
            }
    }
//    Row(
//        horizontalArrangement = Arrangement.Center,
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(bottom = 5.dp)
//    ) {
//        Spacer(modifier = Modifier.weight(1F))
//        State.Brush::class.sealedSubclasses.forEach { brushClass ->
//            val brushObject = brushClass.objectInstance!!
//            ToggleStillButton(
//                brushObject.drawable,
//                state.selectedBrush == brushObject,
//                onClick = {
//                    listener(Msg.SelectBrush(brushObject))
//                }
//            )
//            Spacer(modifier = Modifier.weight(1F))
//        }
//    }
}

private val State.Brush.drawable: Int
    get() = when(this) {
        State.Brush.Pen -> R.drawable.ic_sf_hand_draw_fill
        State.Brush.Eraser -> R.drawable.ic_eraser
    }
