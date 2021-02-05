package com.well.androidApp.ui.composableScreens.call

import androidx.compose.foundation.Image
import androidx.compose.foundation.InteractionState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.well.androidApp.R
import com.well.sharedMobile.ViewConstants
import com.well.sharedMobile.puerh.call.CallFeature

@Composable
fun ColumnScope.SmallVideoView(
    state: CallFeature.State,
    listener: (CallFeature.Msg) -> Unit,
    modifier: Modifier,
) {
    val videContext = state.localVideoContext ?: return
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .align(Alignment.End)
            .padding(10.dp)
            .offset(y = ViewConstants.CallScreen.BottomBar.CallButtonOffset.dp)
    ) {
        Box(
            modifier = Modifier
                .height(200.dp)
                .aspectRatio(1080F / 1920)
        ) {
            VideoView(
                videContext,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(45.dp)
                .clickable(
                    onClick = {
                        listener(state.localDeviceState.toggleIsFrontCameraMsg())
                    },
                    interactionState = remember { InteractionState() },
                    indication = rememberRipple(
                        radius = 22.dp,
                    ),
                )
        ) {
            Image(vectorResource(R.drawable.ic_flip_cam), contentDescription = null)
        }
    }
}