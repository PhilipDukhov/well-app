package com.well.androidApp.ui.composableScreens.call

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.call.drawing.DrawingContent
import com.well.androidApp.ui.composableScreens.call.drawing.DrawingPanel
import com.well.androidApp.ui.composableScreens.πCustomViews.UserProfileImage
import com.well.serverModels.Size
import com.well.serverModels.User
import com.well.sharedMobile.ViewConstants.CallScreen.BottomBar.CallButtonOffset
import com.well.sharedMobile.ViewConstants.CallScreen.CallButtonRadius
import com.well.sharedMobile.puerh.call.CallFeature.Msg
import com.well.sharedMobile.puerh.call.CallFeature.State
import com.well.sharedMobile.puerh.call.CallFeature.State.Status
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature
import dev.chrisbanes.accompanist.coil.CoilImage
import dev.chrisbanes.accompanist.insets.navigationBarsPadding
import dev.chrisbanes.accompanist.insets.statusBarsPadding

@Composable
fun CallScreen(
    state: State,
    listener: (Msg) -> Unit,
) = ConstraintLayout(
    modifier = Modifier
        .fillMaxSize()
) {
    val ongoing = state.status == Status.Ongoing
    val callButtonOffset = if (ongoing) (CallButtonRadius - CallButtonOffset).dp else 0.dp
    //should be inside ConstraintLayoutScope and before it gets called
    fun Modifier.videoContainerModifier(
        position: State.VideoView.Position,
        reference: ConstrainedLayoutReference,
        bottomTopReference: ConstrainedLayoutReference,
    ) = constrainAs(reference) {
            when (position) {
                State.VideoView.Position.FullScreen -> Unit
                State.VideoView.Position.Minimized -> {
                    bottom.linkTo(bottomTopReference.top)
                    end.linkTo(parent.end)
                }
            }
        }.run {
            padding(
                when (position) {
                    State.VideoView.Position.FullScreen -> {
                        0.dp
                    }
                    State.VideoView.Position.Minimized -> {
                        10.dp
                    }
                }
            ).offset(y = when (position) {
                State.VideoView.Position.FullScreen -> {
                    0.dp
                }
                State.VideoView.Position.Minimized -> {
                    callButtonOffset
                }
            })
        }
    val (localVideoView, remoteVideoView, profileImage, nameContainer, bottomView) = createRefs()
    Image(
        vectorResource(R.drawable.ic_call_background),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .fillMaxSize()
    )
    UserProfileImage(
        state.user,
        squareCircleShaped = !ongoing,
        modifier = Modifier
            .constrainAs(profileImage) {
                centerHorizontallyTo(parent)
            }
            .run {
                if (ongoing) {
                    fillMaxSize()
                } else {
                    fillMaxWidth(0.6F)
                        .padding(top = 30.dp)
                        .statusBarsPadding()
                }
            }
    )
    Column(
        modifier = Modifier
            .constrainAs(nameContainer) {
                centerHorizontallyTo(parent)
                if (ongoing) {
                    linkTo(parent.top, bottomView.top)
                } else {
                    top.linkTo(profileImage.bottom, margin = 20.dp)
                }
            }
            .fillMaxWidth(0.9F)
    ) {
        FullNameText(
            user = state.user,
            modifier = Modifier
                .fillMaxWidth()
        )
        if (!ongoing) {
            Text(
                state.status.stringRepresentation,
                color = androidx.compose.ui.graphics.Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body1,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
    state.remoteVideoView?.apply {
        VideoViewContainer(
            view = this,
            onFlip = null,
            containerModifier = Modifier.videoContainerModifier(
                position,
                reference = remoteVideoView,
                bottomTopReference = bottomView,
            ),
            modifier = Modifier
                .videoModifier(position = position),
        )
    }
    state.localVideoView?.apply {
        VideoViewContainer(
            view = this,
            onFlip = when (position) {
                State.VideoView.Position.FullScreen -> null
                State.VideoView.Position.Minimized -> {
                    { listener(state.localDeviceState.toggleIsFrontCameraMsg()) }
                }
            },
            containerModifier = Modifier.videoContainerModifier(
                position,
                reference = localVideoView,
                bottomTopReference = bottomView,
            ),
            modifier = Modifier
                .videoModifier(position = position),
        )
    }
    DrawingContent(
        state.drawingState,
        { listener(Msg.DrawingMsg(it)) },
        enabled = state.controlSet == State.ControlSet.Drawing,
        modifier = Modifier.fillMaxSize()
    )
    when (state.controlSet) {
        State.ControlSet.Call -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .constrainAs(bottomView) {
                        bottom.linkTo(parent.bottom)
                    }
                    .fillMaxWidth()
                    .navigationBarsPadding(bottom = !ongoing)
            ) {
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
        State.ControlSet.Drawing -> {
            DrawingPanel(state.drawingState) {
                listener(Msg.DrawingMsg(it))
            }
        }
    }
}

@Composable
fun FullNameText(
    user: User,
    modifier: Modifier,
) =
    Text(
        user.fullName,
        color = androidx.compose.ui.graphics.Color.White,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.h4,
        modifier = modifier
            .fillMaxWidth(0.9F)
    )

private fun Modifier.videoModifier(
    position: State.VideoView.Position,
) = when (position) {
    State.VideoView.Position.FullScreen -> {
        fillMaxSize()
    }
    State.VideoView.Position.Minimized -> {
        fillMaxWidth(0.3F)
            .aspectRatio(1080F / 1920)
    }
}