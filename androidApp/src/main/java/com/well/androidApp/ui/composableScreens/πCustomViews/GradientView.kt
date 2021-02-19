package com.well.androidApp.ui.composableScreens.πCustomViews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorStop
import androidx.compose.ui.platform.LocalDensity
import com.well.androidApp.ui.composableScreens.πExt.denormalize
import com.well.androidApp.ui.composableScreens.πExt.toColor
import com.well.androidApp.ui.composableScreens.πExt.toOffset
import com.well.sharedMobile.utils.Gradient

@Suppress("CHANGING_ARGUMENTS_EXECUTION_ORDER_FOR_NAMED_VARARGS")
@Composable
fun GradientView(
    gradient: Gradient,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(gradient.backgroundColor.toColor())
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .matchParentSize()
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(gradient.overlayOpacity)
                    .background(
                        Brush.linearGradient(
                            colorStops = gradient.stops.map(Gradient.Stop::toColorStop)
                                .toTypedArray(),
                            start = gradient.startPoint.toOffset()
                                .denormalize(constraints),
                            end = gradient.endPoint.toOffset()
                                .denormalize(constraints),
                        )
                    )

            )
        }
    }
}

private fun Gradient.Stop.toColorStop() =
    ColorStop(location, color.toColor())