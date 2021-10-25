package com.well.modules.androidUi.customViews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import com.well.modules.androidUi.ext.denormalize
import com.well.modules.androidUi.ext.toColor
import com.well.modules.androidUi.ext.toOffset
import com.well.modules.utils.viewUtils.Gradient

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
                            colorStops = gradient.stops.map { it.location to it.color.toColor() }
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