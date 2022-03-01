package com.well.modules.androidUi.customViews

import com.well.modules.androidUi.ext.toColor
import com.well.modules.androidUi.theme.captionLight
import com.well.modules.models.Color
import com.well.modules.models.User
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ReviewInfoView(
    reviewInfo: User.ReviewInfo,
    viewAll: () -> Unit,
) {
    Control(onClick = viewAll) {
        Row {
            Stars(reviewInfo)
            Spacer(modifier = Modifier.weight(1f))
            ReviewsText(reviewInfo)
        }
    }
}

@Composable
fun RowScope.ReviewInfoView(
    reviewInfo: User.ReviewInfo,
) {
    Stars(reviewInfo)
    Spacer(modifier = Modifier.weight(1f))
    ReviewsText(reviewInfo)
}

@Composable
fun ColumnScope.ReviewInfoView(
    reviewInfo: User.ReviewInfo,
) {
    Stars(reviewInfo)
    Spacer(modifier = Modifier.weight(1f))
    ReviewsText(reviewInfo)
}

@Composable
private fun Stars(
    reviewInfo: User.ReviewInfo,
) {
    val value = reviewInfo.currentUserReview?.value?.toDouble() ?: reviewInfo.average
    Row {
        for (star in 1..5) {
            Icon(
                Icons.Rounded.Star,
                contentDescription = null,
                tint = (
                        if (star <= value)
                            if (reviewInfo.currentUserReview !== null)
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
    reviewInfo: User.ReviewInfo,
) {
    Text(
        text = "reviews (${reviewInfo.count})",
        color = Color.MediumBlue.toColor(),
        style = MaterialTheme.typography.captionLight,
    )
}