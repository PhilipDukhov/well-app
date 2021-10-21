package com.well.androidApp.ui.customViews

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.well.androidApp.ui.ext.backgroundKMM
import com.well.androidApp.ui.ext.toColor
import com.well.modules.models.Color
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.CompositionLocalProvider

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
fun BoxScope.InactiveOverlay(showActivityIndicator: Boolean = true, content: @Composable () -> Unit = {}) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .matchParentSize()
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