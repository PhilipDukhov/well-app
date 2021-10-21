package com.well.androidApp.ui.composableScreens.call

import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.call.drawing.DrawingContent
import com.well.androidApp.ui.composableScreens.call.drawing.DrawingPanel
import com.well.androidApp.ui.customViews.ProfileImage
import com.well.androidApp.ui.customViews.controlMinSize
import com.well.androidApp.ui.ext.Image
import com.well.androidApp.ui.ext.visibility
import com.well.androidApp.ui.ext.widthDp
import com.well.modules.models.User
import com.well.sharedMobile.puerh.call.CallFeature.Msg
import com.well.sharedMobile.puerh.call.CallFeature.State
import com.well.sharedMobile.puerh.call.CallFeature.State.Status
import com.well.sharedMobile.utils.ViewConstants.CallScreen.BottomBar.CallButtonOffset
import com.well.sharedMobile.utils.ViewConstants.CallScreen.CallButtonRadius
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature.Msg as DrawingMsg
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding

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
                    minimizedVideoContainerPadding
                }
            }
        ).offset(
            y = when (position) {
                State.VideoView.Position.FullScreen -> {
                    0.dp
                }
                State.VideoView.Position.Minimized -> {
                    callButtonOffset
                }
            }
        )
    }
    val (localVideoView, remoteVideoView, profileImage, nameContainer, bottomView) = createRefs()

    Image(
        painterResource(R.drawable.ic_call_background),
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .fillMaxSize()
    )
    ProfileImage(
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
                    fillMaxWidth(0.6f)
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
            .fillMaxWidth(0.9f)
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
        listener = listener::invokeDrawingMsg,
        enabled = state.controlSet == State.ControlSet.Drawing,
        onSizeChanged = {
            listener.invokeDrawingMsg(DrawingMsg.UpdateLocalVideoContainerSize(it))
        },
        modifier = Modifier
            .fillMaxSize()
            .visibility(state.drawingState.image == null)
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .constrainAs(bottomView) {
                bottom.linkTo(parent.bottom)
            }
            .fillMaxWidth()
            .navigationBarsPadding(bottom = !ongoing)
            .visibility(state.controlSet == State.ControlSet.Call)
    ) {
        if (state.viewPoint == State.ViewPoint.Mine) {
            FlipCameraButton(
                onFlip = {
                    listener(state.localDeviceState.toggleIsFrontCameraMsg())
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(minimizedVideoContainerPadding)
                    .offset(
                        x = (controlMinSize - LocalContext.current.resources.displayMetrics.widthDp * minimizedVideoPart) / 2,
                        y = callButtonOffset
                    )
            )
        }
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
    DrawingPanel(
        state.drawingState,
        modifier = Modifier
            .visibility(state.controlSet == State.ControlSet.Drawing)
    ) {
        listener(Msg.DrawingMsg(it))
    }
}

@Composable
fun FullNameText(
    user: User,
    modifier: Modifier,
) = Text(
    user.fullName,
    color = androidx.compose.ui.graphics.Color.White,
    textAlign = TextAlign.Center,
    style = MaterialTheme.typography.h4,
    modifier = modifier
        .fillMaxWidth(0.9f)
)

private fun Modifier.videoModifier(
    position: State.VideoView.Position,
) = when (position) {
    State.VideoView.Position.FullScreen -> {
        fillMaxSize()
    }
    State.VideoView.Position.Minimized -> {
        fillMaxWidth(minimizedVideoPart)
            .aspectRatio(1080f / 1920)
    }
}

private const val minimizedVideoPart = 0.3f
private val minimizedVideoContainerPadding = 10.dp

private fun ((Msg) -> Unit).invokeDrawingMsg(msg: DrawingMsg) =
    invoke(Msg.DrawingMsg(msg))