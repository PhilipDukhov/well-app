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
import com.well.serverModels.User
import dev.chrisbanes.accompanist.insets.systemBarsPadding

@Composable
fun CallScreen(user: User) =
    Box {
        Image(
            vectorResource(R.drawable.ic_call_background),
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
        )
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
                vectorResource(user.drawable),
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
                user.fullName,
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h4,
                modifier = Modifier
                    .fillMaxWidth(0.9F)
                    .padding(bottom = 11.dp)
            )
            Text(
                "Calling...",
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body1,
                modifier = Modifier
                    .fillMaxWidth(0.9F)
            )
            Spacer(modifier = Modifier.weight(1f))
            Row {
                Spacer(modifier = Modifier.weight(1f))
                CallDownButton {

                }
                Spacer(modifier = Modifier.weight(2f))
                CallUpButton {

                }
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(
                modifier = Modifier
                    .height(32.dp)
            )
        }
    }

@Composable
private fun CallDownButton(onClick: () -> Unit) =
    CallButton(
        R.drawable.ic_phone_down,
        Pink,
        onClick
    )

@Composable
private fun CallUpButton(onClick: () -> Unit) =
    CallButton(
        R.drawable.ic_phone_up,
        Green,
        onClick,
    )

@Composable
private fun CallButton(
    drawable: Int,
    background: Color,
    onClick: () -> Unit,
) =
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick)
    ) {
        Image(vectorResource(drawable))
    }