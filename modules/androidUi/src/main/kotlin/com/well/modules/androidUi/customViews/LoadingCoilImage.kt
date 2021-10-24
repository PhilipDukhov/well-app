package com.well.modules.androidUi.customViews

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter

@Composable
fun LoadingCoilImage(
    data: Any,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    successProgressIndicatorNeeded: Boolean = false,
) {
    val painter = rememberImagePainter(
        data,
        builder = {
            crossfade(true)
        }
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Image(
            painter,
            contentDescription = null,
            contentScale = contentScale,
        )
        if (
            painter.state is ImagePainter.State.Loading
            || (painter.state is ImagePainter.State.Success && successProgressIndicatorNeeded)
        ) {
            ProgressIndicator()
        }
    }
}

@Composable
private fun ProgressIndicator() =
    CircularProgressIndicator(
        Modifier
            .fillMaxWidth(0.7F)
            .aspectRatio(1f)
    )