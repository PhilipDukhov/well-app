package com.well.modules.androidUi.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@Composable
fun LoadingCoilImage(
    data: Any,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    successProgressIndicatorNeeded: Boolean = false,
) {
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        remember(data) {
            ImageRequest.Builder(context)
                .data(data)
                .memoryCacheKey(data as? String)
                .crossfade(true)
                .build()
        }
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Image(
            painter,
            contentDescription = null,
            contentScale = contentScale,
            modifier = Modifier.matchParentSize()
        )
        val progressIndicatorNeeded by remember {
            derivedStateOf {
                painter.state is AsyncImagePainter.State.Loading
                        || (painter.state is AsyncImagePainter.State.Success && successProgressIndicatorNeeded)
            }
        }
        if (progressIndicatorNeeded) {
            CircularProgressIndicator(
                Modifier
                    .fillMaxWidth(0.7F)
                    .aspectRatio(1f)
            )
        }
    }
}