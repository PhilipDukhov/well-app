package com.well.androidApp.ui.composableScreens.πCustomViews

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import coil.compose.rememberImagePainter

@Composable
fun rememberDrawablePainter(@DrawableRes id: Int): Painter {
    val context = LocalContext.current
    val drawable = remember(id) {
        ContextCompat.getDrawable(context, id)
    }
    return rememberImagePainter(data = drawable)
}
