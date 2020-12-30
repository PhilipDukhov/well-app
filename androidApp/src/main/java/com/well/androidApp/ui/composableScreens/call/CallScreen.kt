package com.well.androidApp.ui.composableScreens.call

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.drawable
import com.well.androidApp.ui.composableScreens.theme.Color.Green
import com.well.androidApp.ui.composableScreens.theme.Color.Pink
import com.well.shared.puerh.call.CallFeature
import com.well.shared.puerh.call.CallFeature.Msg
import com.well.shared.puerh.call.CallFeature.State.Status
import dev.chrisbanes.accompanist.insets.systemBarsPadding

@Composable
fun CallScreen(
    state: CallFeature.State,
    listener: (Msg) -> Unit,
) =
    Box {
        Image(
            vectorResource(R.drawable.ic_call_background),
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
        )
        state.localVideoContext?.let { context ->
            SurfaceView(
                context,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .systemBarsPadding()
        ) {
            Spacer(
                modifier = Modifier
                    .height(16.dp)
            )
            Image(
                vectorResource(state.user.drawable),
                colorFilter = ColorFilter.tint(Color.Blue),
                modifier = Modifier
                    .fillMaxWidth(0.6F)
                    .aspectRatio(1F)
                    .clip(CircleShape)
            )
            Spacer(
                modifier = Modifier
                    .height(40.dp)
            )
            Text(
                state.user.fullName,
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h4,
                modifier = Modifier
                    .fillMaxWidth(0.9F)
                    .padding(bottom = 11.dp)
            )
            Text(
                when (state.status) {
                    Status.Calling -> "Calling..."
                    Status.Incoming -> "Call"
                    Status.Connecting -> "Connecting"
                    Status.Ongoing -> "00:05"
                },
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body1,
                modifier = Modifier
                    .fillMaxWidth(0.9F)
            )
            Spacer(modifier = Modifier.weight(1f))
            state.remoteVideoContext?.let { context ->
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .background(Color.Red)
                        .aspectRatio(1080F/1920)
                        .align(Alignment.End)
                        .padding(10.dp)
                ) {
                    SurfaceView(
                        context,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            }
            Row {
                Spacer(modifier = Modifier.weight(1f))
                CallDownButton {
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
            Spacer(
                modifier = Modifier
                    .height(32.dp)
            )
        }
    }