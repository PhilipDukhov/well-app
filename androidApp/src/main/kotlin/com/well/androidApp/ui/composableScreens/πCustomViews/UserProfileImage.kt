package com.well.androidApp.ui.composableScreens.πCustomViews

import com.well.androidApp.ui.composableScreens.πExt.backgroundKMM
import com.well.modules.models.Color
import com.well.modules.models.User
import com.well.modules.utils.sharedImage.SharedImage
import com.well.modules.utils.sharedImage.profileImage
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale

@Suppress("NAME_SHADOWING")
@Composable
fun UserProfileImage(
    user: User?,
    modifier: Modifier = Modifier,
    squareCircleShaped: Boolean = true,
    contentScale: ContentScale = ContentScale.Crop,
) = UserProfileImage(
    image = user?.profileImage(),
    modifier,
    squareCircleShaped,
    contentScale,
)

@Suppress("NAME_SHADOWING")
@Composable
fun UserProfileImage(
    image: SharedImage?,
    modifier: Modifier = Modifier,
    squareCircleShaped: Boolean = true,
    contentScale: ContentScale = ContentScale.Crop,
) = if (squareCircleShaped) {
    modifier
        .clip(CircleShape)
        .aspectRatio(1F)
} else {
    modifier
}.let {  modifier ->
    image?.let {
        println("LoadingCoilImage reload ")
        LoadingCoilImage(it.coilDataAny, contentScale, modifier)
    } ?: Box(
        modifier = modifier.backgroundKMM(Color.LightGray)
    )
}