package com.well.modules.androidUi.customViews

import com.well.modules.androidUi.ext.backgroundKMM
import com.well.modules.models.Color
import com.well.modules.models.User
import com.well.modules.utils.sharedImage.SharedImage
import com.well.modules.utils.sharedImage.profileImage
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale

@Suppress("NAME_SHADOWING")
@Composable
fun ProfileImage(
    user: User?,
    modifier: Modifier = Modifier,
    squareCircleShaped: Boolean = true,
    contentScale: ContentScale = ContentScale.Crop,
) = ProfileImage(
    image = user?.profileImage(),
    modifier = modifier,
    initials = user?.initials,
    squareCircleShaped = squareCircleShaped,
    contentScale = contentScale,
)

@Suppress("NAME_SHADOWING")
@Composable
fun ProfileImage(
    image: SharedImage?,
    modifier: Modifier = Modifier,
    initials: String? = null,
    squareCircleShaped: Boolean = true,
    contentScale: ContentScale = ContentScale.Crop,
) = if (squareCircleShaped) {
    modifier
        .clip(CircleShape)
        .aspectRatio(1F)
} else {
    modifier
}.let { modifier ->
    image?.let {
        LoadingCoilImage(
            it.coilDataAny,
            modifier,
            contentScale,
        )
    } ?: BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier.backgroundKMM(Color.LightGray)
    ) {
        initials?.let { initials ->
            FillFullSizeText(
                text = initials,
                baseStyle = MaterialTheme.typography.body2,
            )
        }
    }
}