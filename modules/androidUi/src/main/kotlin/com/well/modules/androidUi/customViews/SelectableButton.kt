package com.well.modules.androidUi.customViews

import com.well.modules.androidUi.ext.backgroundKMM
import com.well.modules.androidUi.ext.borderKMM
import com.well.modules.androidUi.ext.toColor
import com.well.modules.models.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun SelectableButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = MaterialTheme.shapes.medium
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(shape)
            .borderKMM(
                if (selected) 0.dp else 2.dp,
                color = Color.LightGray,
                shape = shape,
            )
            .backgroundKMM(
                if (selected) Color.Green else Color.Transparent,
            )
            .clickable(onClick),
    ) {
        CompositionLocalProvider(
            LocalContentColor provides (if (selected) Color.White else Color.DarkGrey).toColor()
        ) {
            content()
        }
    }
}