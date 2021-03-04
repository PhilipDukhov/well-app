package com.well.androidApp.ui.composableScreens.call.drawing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.well.androidApp.ui.composableScreens.πExt.backgroundKMM
import com.well.androidApp.ui.composableScreens.πExt.borderKMM
import com.well.serverModels.Color
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature.Msg
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature.State
import dev.chrisbanes.accompanist.insets.navigationBarsPadding

@Composable
fun BottomPanel(
    state: State,
    listener: (Msg) -> Unit,
    modifier: Modifier = Modifier,
) = Column(
    modifier = modifier
        .backgroundKMM(Color.MineShaft)
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
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(
                                bounded = false,
                                radius = 20.dp,
                            )
                        )
                        .weight(1f)
                        .aspectRatio(1F)
                        .padding(10.dp)
                        .borderKMM(
                            width = 2.dp,
                            color = if (selected) Color.AzureRadiance else Color.Transparent,
                            shape = CircleShape,
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .padding(3.dp)
                            .clip(CircleShape)
                            .backgroundKMM(color)
                            .fillMaxSize()
                    )
                }
            }
    }
// brush/eraser
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

//private val State.Brush.drawable: Int
//    get() = when(this) {
//        State.Brush.Pen -> R.drawable.ic_sf_hand_draw_fill
//        State.Brush.Eraser -> R.drawable.ic_eraser
//    }
