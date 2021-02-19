package com.well.androidApp.ui.composableScreens.πCustomViews

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import com.well.androidApp.ui.composableScreens.πExt.backgroundKMM
import com.well.serverModels.Color
import com.well.serverModels.User
import com.well.sharedMobile.utils.ImageContainer
import com.well.sharedMobile.utils.SharedImage
import com.well.sharedMobile.utils.UrlImage
import com.well.sharedMobile.utils.profileImage

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
        LoadingCoilImage(it.coilDataAny, contentScale, modifier)
    } ?: Box(
        modifier = modifier.backgroundKMM(Color.LightGray)
    )
}