package com.well.androidApp.ui.composableScreens.call

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.πCustomViews.UserProfileImage
import com.well.androidApp.ui.composableScreens.πExt.backgroundW
import com.well.serverModels.Color
import com.well.sharedMobile.ViewConstants.CallScreen.BottomBar.CallButtonOffset
import com.well.sharedMobile.ViewConstants.CallScreen.CallButtonRadius
import com.well.sharedMobile.puerh.call.CallFeature.Msg
import com.well.sharedMobile.puerh.call.CallFeature.State
import com.well.sharedMobile.puerh.call.CallFeature.State.Status
import dev.chrisbanes.accompanist.insets.navigationBarsPadding
import dev.chrisbanes.accompanist.insets.statusBarsPadding
import dev.chrisbanes.accompanist.insets.systemBarsPadding

@Composable
fun CallScreen(
    state: State,
    listener: (Msg) -> Unit,
) = Box {
    state.remoteVideoContext?.let { context ->
        VideoView(
            context,
            modifier = Modifier
                .fillMaxSize()
        )
    } ?: run {
        if (state.status == Status.Ongoing) {
            UserProfileImage(
                state.user,
                squareCircleShaped = false,
                modifier = Modifier
                    .fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .backgroundW(Color.InactiveOverlay)
            )
            FullNameText(
                user = state.user,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp)
                    .systemBarsPadding()
            )
        } else {
            Image(
                vectorResource(R.drawable.ic_call_background),
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
    val ongoing = state.status == Status.Ongoing
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(bottom = !ongoing)
    ) {
        val callButtonOffset = if (ongoing) (CallButtonRadius - CallButtonOffset).dp else 0.dp
        if (!ongoing) {
            IncomingInfo(
                state,
                modifier = Modifier
                    .statusBarsPadding()
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        SmallVideoView(state, listener, modifier = Modifier.offset(y = callButtonOffset))
        Row(
            modifier = Modifier
                .padding(bottom = if (ongoing) 0.dp else 10.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            CallDownButton(
                modifier = Modifier
                    .offset(y = callButtonOffset)
            ) {
                listener.invoke(Msg.End)
            }
            when (state.status) {
                Status.Calling -> Unit
                Status.Incoming -> {
                    Spacer(modifier = Modifier.weight(2f))
                    CallUpButton {
                        listener.invoke(Msg.Accept)
                    }
                }
                Status.Connecting -> Unit
                Status.Ongoing -> Unit
            }
            Spacer(modifier = Modifier.weight(1f))
        }
        if (ongoing) {
            BottomBar(state, listener)
        }
    }
}

@Composable
private fun ColumnScope.SmallVideoView(
    state: State,
    listener: (Msg) -> Unit,
    modifier: Modifier,
) {
    val videContext = state.localVideoContext ?: return
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .align(Alignment.End)
            .padding(10.dp)
            .offset(y = CallButtonOffset.dp)
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
                        listener(state.deviceState.toggleIsFrontCameraMsg())
                    },
                    indication = rememberRipple(
                        radius = 22.dp,
                    ),
                )
        ) {
            Image(vectorResource(R.drawable.ic_flip_cam))
        }
    }
}