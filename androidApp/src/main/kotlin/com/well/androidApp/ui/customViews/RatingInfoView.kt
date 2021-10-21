package com.well.androidApp.ui.customViews

import com.well.androidApp.R
import com.well.androidApp.ui.theme.captionLight
import com.well.androidApp.ui.ext.toColor
import com.well.modules.models.Color
import com.well.modules.models.User
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight


@Composable
fun RatingInfoView(
    ratingInfo: User.RatingInfo,
    viewAll: () -> Unit,
) {
    Control(onClick = viewAll) {
        Row {
            Stars(ratingInfo)
            Spacer(modifier = Modifier.weight(1f))
            ReviewsText(ratingInfo)
        }
    }
}

@Composable
fun RowScope.RatingInfoView(
    ratingInfo: User.RatingInfo,
) {
    Stars(ratingInfo)
    Spacer(modifier = Modifier.weight(1f))
    ReviewsText(ratingInfo)
}

@Composable
fun ColumnScope.RatingInfoView(
    ratingInfo: User.RatingInfo,
) {
    Stars(ratingInfo)
    Spacer(modifier = Modifier.weight(1f))
    ReviewsText(ratingInfo)
}

@Composable
private fun Stars(
    ratingInfo: User.RatingInfo,
) {
    val value = ratingInfo.currentUserRating?.value?.toDouble() ?: ratingInfo.average
    Row {
        for (star in 1..5) {
            Icon(
                painterResource(R.drawable.ic_round_star_24),
                contentDescription = "",
                tint = (
                        if (star <= value)
                            if (ratingInfo.currentUserRating !== null)
                                Color.Supernova
                            else
                                Color.Green
                        else
                            Color.LightGray
                        ).toColor(),
            )
        }
    }
}

@Composable
private fun ReviewsText(
    ratingInfo: User.RatingInfo,
) {
    Text(
        text = "reviews (${ratingInfo.count})",
        color = Color.MediumBlue.toColor(),
        style = MaterialTheme.typography.captionLight,
    )
}