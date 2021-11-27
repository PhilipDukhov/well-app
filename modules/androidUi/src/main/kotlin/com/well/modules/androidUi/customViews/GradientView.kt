package com.well.modules.androidUi.customViews

import com.well.modules.androidUi.ext.denormalize
import com.well.modules.androidUi.ext.toColor
import com.well.modules.androidUi.ext.toOffset
import com.well.modules.utils.viewUtils.Gradient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush

@Composable
fun GradientView(
    gradient: Gradient,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(gradient.backgroundColor.toColor())
                .alpha(gradient.overlayOpacity)
                .background(
                    Brush.linearGradient(
                        colorStops = gradient.stops
                            .map { it.location to it.color.toColor() }
                            .toTypedArray(),
                        start = gradient.startPoint
                            .toOffset()
                            .denormalize(constraints),
                        end = gradient.endPoint
                            .toOffset()
                            .denormalize(constraints),
                    )
                )

        )
    }
}