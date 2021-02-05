package com.well.androidApp.ui.composableScreens.πCustomViews

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import dev.chrisbanes.accompanist.coil.CoilImage

@Composable
fun LoadingCoilImage(
    data: Any,
    contentScale: ContentScale,
    modifier: Modifier,
) = CoilImage(
    data = data,
    contentDescription = null,
    loading = {
        Box {
            CircularProgressIndicator(
                Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(0.7F)
            )
        }
    },
    contentScale = contentScale,
    modifier = modifier,
)