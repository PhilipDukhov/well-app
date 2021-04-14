package com.well.androidApp.ui.composableScreens.πCustomViews

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.well.androidApp.ui.composableScreens.πExt.backgroundKMM
import com.well.androidApp.ui.composableScreens.πExt.toColor
import com.well.modules.models.Color

@Composable
fun InactiveOverlay(showActivityIndicator: Boolean = true) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .backgroundKMM(Color.InactiveOverlay)
    ) {
        if (showActivityIndicator) {
            CircularProgressIndicator(
                color = Color.White.toColor(),
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(0.1f)
            )
        }
    }
}