package com.well.androidApp.ui.customViews

import com.well.androidApp.R
import com.well.androidApp.ui.ext.toColor
import com.well.modules.models.Color
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource

@Composable
fun ToggleFavoriteButton(
    favorite: Boolean,
    toggle: () -> Unit,
) {
    Control(onClick = toggle) {
        Icon(
            painterResource(
                if (favorite)
                    R.drawable.ic_round_favorite_24
                else
                    R.drawable.ic_round_favorite_border_24
            ),
            contentDescription = "",
            tint = (if (favorite) Color.Green else Color.LightGray).toColor(),
        )
    }
}