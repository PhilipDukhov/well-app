package com.well.androidApp.ui.composableScreens.call

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.well.androidApp.ui.composableScreens.πCustomViews.UserProfileImage
import com.well.serverModels.User
import com.well.sharedMobile.puerh.call.CallFeature

@Composable
fun IncomingInfo(
    state: CallFeature.State,
    modifier: Modifier,
) = Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier,
) {
    Spacer(
        modifier = Modifier
            .height(16.dp)
    )
    UserProfileImage(
        state.user,
        modifier = Modifier
            .fillMaxWidth(0.6F)
    )
    Spacer(
        modifier = Modifier
            .preferredHeight(40.dp)
    )
    FullNameText(
        state.user,
        modifier = Modifier.padding(bottom = 11.dp)
    )
    Text(
        state.status.stringRepresentation,
        color = Color.White,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.body1,
        modifier = Modifier
            .fillMaxWidth(0.9F)
    )
}

@Composable
fun FullNameText(
    user: User,
    modifier: Modifier,
) =
    Text(
        user.fullName,
        color = Color.White,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.h4,
        modifier = modifier
            .fillMaxWidth(0.9F)
    )