package com.well.androidApp.ui.composableScreens.experts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.well.androidApp.ui.composableScreens.πCustomViews.UserProfileImage
import com.well.androidApp.ui.composableScreens.πExt.Image
import com.well.modules.models.User
import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.πCustomViews.Control
import com.well.androidApp.ui.composableScreens.πExt.toColor
import com.well.modules.models.Color

private val padding = 16.dp

@Composable
fun UserCell(
    user: User,
    onSelect: () -> Unit,
    onCall: () -> Unit,
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onSelect)
        .padding(padding)
) {
    UserProfileImage(
        user,
        modifier = Modifier
            .size(40.dp)
    )
    Spacer(modifier = Modifier.width(padding))
    Text(
        text = user.fullName,
        maxLines = 1,
    )
    Spacer(modifier = Modifier.weight(1f))
    Control(onClick = onCall) {
        Image(
            painterResource(R.drawable.ic_baseline_call_24),
            colorFilter = ColorFilter.tint(Color.Green.toColor())
        )
    }
}