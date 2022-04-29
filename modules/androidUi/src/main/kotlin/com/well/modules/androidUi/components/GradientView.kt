package com.well.modules.androidUi.components

import com.well.modules.androidUi.ext.denormalize
import com.well.modules.androidUi.ext.toColor
import com.well.modules.androidUi.ext.toOffset
import com.well.modules.utils.viewUtils.Gradient
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope

@Composable
fun GradientView(
    gradient: Gradient,
    modifier: Modifier = Modifier,
) {
    Box(modifier.gradientBackground(gradient))
}

fun Modifier.gradientBackground(gradient: Gradient) =
    drawBehind {
        drawGradient(gradient)
    }

fun DrawScope.drawGradient(gradient: Gradient, alpha: Float = 1f) {
    drawRect(
        color = gradient.backgroundColor.toColor(),
        alpha = alpha,
    )
    drawRect(
        Brush.linearGradient(
            colorStops = gradient.stops
                .map { it.location to it.color.toColor() }
                .toTypedArray(),
            start = gradient.startPoint
                .toOffset()
                .denormalize(size),
            end = gradient.endPoint
                .toOffset()
                .denormalize(size),
        ),
        alpha = gradient.overlayOpacity * alpha,
    )
}