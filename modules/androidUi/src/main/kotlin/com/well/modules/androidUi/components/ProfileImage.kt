package com.well.modules.androidUi.components

import com.well.modules.androidUi.ext.backgroundKMM
import com.well.modules.androidUi.ext.thenOrNull
import com.well.modules.androidUi.ext.toColor
import com.well.modules.models.Color
import com.well.modules.models.User
import com.well.modules.utils.viewUtils.sharedImage.SharedImage
import com.well.modules.utils.viewUtils.sharedImage.profileImage
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.ContentScale
import kotlin.math.sqrt

@Composable
fun ProfileImage(
    user: User?,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    aspectRatio: Float? = 1f,
    contentScale: ContentScale = ContentScale.Crop,
) {
    ProfileImage(
        image = user?.profileImage(),
        modifier = modifier,
        isOnline = user?.isOnline ?: false,
        initials = user?.initials,
        shape = shape,
        aspectRatio = aspectRatio,
        contentScale = contentScale,
    )
}

@Composable
fun ProfileImage(
    image: SharedImage?,
    modifier: Modifier = Modifier,
    isOnline: Boolean = false,
    initials: String? = null,
    shape: Shape = CircleShape,
    aspectRatio: Float? = 1f,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val contentModifier = modifier
        .then(
            if (isOnline && shape == CircleShape) {
                Modifier.drawWithContent {
                    val bigRadius = size.maxDimension / 2
                    val x = bigRadius * 0.67f
                    val y = sqrt(bigRadius * bigRadius - x * x)
                    val center = Offset(
                        x = x + bigRadius,
                        y = y + bigRadius,
                    )
                    val radius = size.height - center.y
                    clipPath(
                        Path().apply {
                            addOval(Rect(offset = Offset.Zero, size = size))
                        },
                    ) {
                        clipPath(
                            Path().apply {
                                addOval(
                                    Rect(center = center, radius = radius * 1.2f)
                                )
                            },
                            clipOp = ClipOp.Difference
                        ) {
                            this@drawWithContent.drawContent()
                        }
                    }
                    drawCircle(Color.Green.toColor(), center = center, radius = radius)
                }
            } else {
                Modifier.clip(shape)
            }
        )
        .thenOrNull(
            aspectRatio?.let(Modifier::aspectRatio)
        )
    if (image != null) {
        LoadingCoilImage(
            image.coilDataAny,
            contentModifier,
            contentScale,
        )
    } else {
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = contentModifier.backgroundKMM(Color.LightGray)
        ) {
            initials?.let { initials ->
                FillFullSizeText(
                    text = initials,
                    baseStyle = MaterialTheme.typography.body2,
                )
            }
        }
    }
}