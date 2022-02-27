package com.well.modules.androidUi.customViews.usersList

import com.well.modules.androidUi.customViews.ProfileImage
import com.well.modules.androidUi.customViews.RatingInfoView
import com.well.modules.androidUi.customViews.ToggleFavoriteButton
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
            Row {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.LightBlue.toColor(),
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
        RatingInfoView(ratingInfo = user.ratingInfo)
        ToggleFavoriteButton(favorite = user.favorite, toggle = onToggleFavorite)
    }
}