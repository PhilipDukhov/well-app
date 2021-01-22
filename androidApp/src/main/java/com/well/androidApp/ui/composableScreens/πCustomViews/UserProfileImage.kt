package com.well.androidApp.ui.composableScreens.πCustomViews

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import com.well.androidApp.ui.composableScreens.πExt.backgroundW
import com.well.serverModels.Color
import com.well.serverModels.User

@Suppress("NAME_SHADOWING")
@Composable
fun UserProfileImage(
    user: User,
    squareCircleShaped: Boolean = true,
    contentScale: ContentScale = ContentScale.Crop,
    modifier: Modifier,
) = if (squareCircleShaped) {
    modifier
        .clip(CircleShape)
        .aspectRatio(1F)
} else {
    modifier
}.let { modifier ->
    user.profileImageUrl?.let {
        LoadingCoilImage(it, contentScale, modifier)
    } ?: Box(
        modifier = modifier.backgroundW(Color.DodgerBlue)
    )
}
