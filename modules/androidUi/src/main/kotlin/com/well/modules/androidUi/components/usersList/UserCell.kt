package com.well.modules.androidUi.components.usersList

import com.well.modules.androidUi.components.ProfileImage
import com.well.modules.androidUi.components.ReviewInfoView
import com.well.modules.androidUi.components.ToggleFavoriteButton
import com.well.modules.androidUi.ext.toColor
import com.well.modules.androidUi.theme.captionLight
import com.well.modules.models.Color
import com.well.modules.models.User
import com.well.modules.utils.viewUtils.countryCodes.countryName
import com.well.modules.utils.viewUtils.countryCodes.localizedDescription
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp

private val padding = 16.dp

@Composable
fun UserCell(
    user: User,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onSelect)
        .padding(padding)
) {
    ProfileImage(
        user,
        modifier = Modifier
            .size(40.dp)
    )
    Spacer(modifier = Modifier.width(padding))
    Column {
        Text(
            text = user.fullName,
            maxLines = 1,
            style = MaterialTheme.typography.caption,
        )

        user.academicRank?.let { academicRank ->
            Text(
                text = academicRank.localizedDescription(),
                maxLines = 1,
                style = MaterialTheme.typography.captionLight,
            )
        }

        user.countryName()?.let { countryName ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.LightBlue.toColor(),
                    modifier = Modifier.clipToBounds()
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            val offset = 4.dp
                            layout(placeable.width - offset.roundToPx(), placeable.height) {
                                placeable.place(-offset.roundToPx(), 0)
                            }
                        }
                )
                Text(
                    text = countryName,
                    style = MaterialTheme.typography.captionLight,
                )
            }
        }
    }
    Spacer(modifier = Modifier.weight(1f))
    Column(horizontalAlignment = Alignment.End) {
        ReviewInfoView(reviewInfo = user.reviewInfo)
        ToggleFavoriteButton(favorite = user.favorite, toggle = onToggleFavorite)
    }
}