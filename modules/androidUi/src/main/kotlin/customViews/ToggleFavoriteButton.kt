package com.well.modules.androidUi.customViews

import com.well.modules.androidUi.ext.toColor
import com.well.modules.models.Color
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.runtime.Composable

@Composable
fun ToggleFavoriteButton(
    favorite: Boolean,
    toggle: () -> Unit,
) {
    Control(onClick = toggle) {
        Icon(
            with(Icons.Rounded) {
                if (favorite) Favorite else FavoriteBorder
            },
            contentDescription = null,
            tint = (if (favorite) Color.Green else Color.LightGray).toColor(),
        )
    }
}