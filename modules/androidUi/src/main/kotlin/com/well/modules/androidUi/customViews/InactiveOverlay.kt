package com.well.modules.androidUi.customViews

import com.well.modules.androidUi.ext.backgroundKMM
import com.well.modules.androidUi.ext.toColor
import com.well.modules.models.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun InactiveOverlay(showActivityIndicator: Boolean = true, content: @Composable () -> Unit = {}) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .backgroundKMM(Color.InactiveOverlay)
    ) {
        content()
        if (showActivityIndicator) {
            CircularProgressIndicator(
                color = Color.White.toColor(),
                modifier = Modifier
                    .fillMaxSize(0.1f)
            )
        }
    }
}

@Composable
fun BoxScope.InactiveOverlay(
    showActivityIndicator: Boolean = false,
    content: @Composable () -> Unit = {},
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .matchParentSize()
            .backgroundKMM(Color.InactiveOverlay)
    ) {
        ProvideTextStyle(
            MaterialTheme.typography.subtitle1.copy(color = Color.White.toColor()),
            content = content
        )
        if (showActivityIndicator) {
            CircularProgressIndicator(
                color = Color.White.toColor(),
                modifier = Modifier
                    .fillMaxSize(0.1f)
            )
        }
    }
}