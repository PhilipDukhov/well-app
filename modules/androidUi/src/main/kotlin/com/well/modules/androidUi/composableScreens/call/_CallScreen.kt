package com.well.modules.androidUi.composableScreens.call

import com.well.modules.androidUi.components.GradientView
import com.well.modules.androidUi.components.ProfileImage
import com.well.modules.androidUi.components.controlMinSize
import com.well.modules.androidUi.composableScreens.call.drawing.DrawingContent
import com.well.modules.androidUi.composableScreens.call.drawing.DrawingPanel
import com.well.modules.androidUi.ext.thenIf
import com.well.modules.androidUi.ext.visibility
import com.well.modules.androidUi.ext.widthDp
import com.well.modules.features.call.callFeature.CallFeature.Msg
import com.well.modules.features.call.callFeature.CallFeature.State
import com.well.modules.features.call.callFeature.CallFeature.State.Status
import com.well.modules.models.User
import com.well.modules.utils.viewUtils.Gradient
import com.well.modules.utils.viewUtils.ViewConstants.CallScreen.BottomBar.CallButtonOffset
import com.well.modules.utils.viewUtils.ViewConstants.CallScreen.CallButtonRadius
import com.well.modules.features.call.callFeature.drawing.DrawingFeature.Msg as DrawingMsg
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout

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
    }
        .padding(
            when (position) {
                State.VideoView.Position.FullScreen -> {
                    0.dp
                }
                State.VideoView.Position.Minimized -> {
                    minimizedVideoContainerPadding
                }
            }
        )
        .offset(
            y = when (position) {
                State.VideoView.Position.FullScreen -> {
                    0.dp
                }
                State.VideoView.Position.Minimized -> {
                    callButtonOffset
                }
            }
        )

    val (localVideoView, remoteVideoView, profileImage, nameContainer, bottomView) = createRefs()

    GradientView(
        gradient = Gradient.CallBackground,
        modifier = Modifier
            .fillMaxSize()
    )
    ProfileImage(
        state.user,
        shape = if (ongoing) RectangleShape else CircleShape,
        aspectRatio = if (ongoing) null else 1f,
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
            contentModifier = Modifier
                .videoModifier(position = position),
            modifier = Modifier.videoContainerModifier(
                position,
                reference = remoteVideoView,
                bottomTopReference = bottomView,
            )
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
            contentModifier = Modifier
                .videoModifier(position = position),
            modifier = Modifier.videoContainerModifier(
                position,
                reference = localVideoView,
                bottomTopReference = bottomView,
            )
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
            .thenIf(condition = !ongoing, Modifier.navigationBarsPadding())
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