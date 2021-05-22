package com.well.androidApp.ui.composableScreens.πCustomViews

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.imageloading.ImageLoadState
import com.well.androidApp.ui.composableScreens.πExt.Image

@Composable
fun LoadingCoilImage(
    data: Any,
    contentScale: ContentScale,
    modifier: Modifier,
) {
    val painter = rememberCoilPainter(data, fadeIn = true)
    Box(modifier = modifier) {
        when (painter.loadState) {
            is ImageLoadState.Loading -> {
                Box {
                    CircularProgressIndicator(
                        Modifier
                            .align(Alignment.Center)
                            .fillMaxSize(0.7F)
                    )
                }
            }
            is ImageLoadState.Error -> {
            }
            is ImageLoadState.Empty -> {

            }
            is ImageLoadState.Success -> {
                Image(
                    painter,
                    contentDescription = null,
                    contentScale = contentScale,
                )
            }
        }
    }
}