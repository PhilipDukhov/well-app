package com.well.androidAppTest

import com.well.androidApp.R
import com.well.modules.androidUi.customViews.GradientView
import com.well.modules.androidUi.ext.denormalize
import com.well.modules.androidUi.ext.toColor
import com.well.modules.androidUi.ext.toOffset
import com.well.modules.models.Point
import com.well.modules.utils.viewUtils.Gradient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Slider
import androidx.compose.material.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.systemBarsPadding
import kotlin.math.hypot

@Composable
fun GradientViewTest() {
    Column(
        Modifier.systemBarsPadding()
    ) {
        GradientViewHeightTest()
//        GradientViewComparisonTest()
    }
}

@Composable
fun ColumnScope.GradientViewComparisonTest() {
    Image(
        painter = painterResource(id = R.drawable.ic_calendar_gradient),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f)
    )
    Spacer(modifier = Modifier.weight(1f))
    GradientViewCalibrator(
        initial = Gradient.Main,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f))
}

@Composable
fun ColumnScope.GradientViewHeightTest() {
    var height by remember { mutableStateOf(100f) }
    Slider(
        value = height,
        valueRange = 100f..375f,
        onValueChange = { height = it },
    )

    Image(
        painter = painterResource(id = R.drawable.ic_calendar_gradient),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
    )
    Spacer(modifier = Modifier.weight(1f))
    GradientView(
        Gradient.Main,
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
    )
}

@Composable
fun GradientViewCalibrator(initial: Gradient, modifier: Modifier) {
    var gradient by remember { mutableStateOf(initial) }
    var movingPoint by remember { mutableStateOf<Int?>(null) }
    val density = LocalDensity.current
    val padding = DpOffset(x = 0.dp, y = 0.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        var sliderAlpha by remember { mutableStateOf(1f) }
        Slider(
            value = sliderAlpha,
            onValueChange = { sliderAlpha = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        Switch(
            checked = sliderAlpha == 1f,
            onCheckedChange = { sliderAlpha = if (it) 1f else 0f },
            modifier = Modifier.align(Alignment.TopCenter)
        )
        BoxWithConstraints(
            Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    fun Offset.normalize(): Point =
                        with(density) {
                            Offset(
                                x - padding.x.toPx(),
                                y - padding.y.toPx()
                            )
                                .normalize(
                                    size.width.toFloat() - padding.x.toPx() * 2,
                                    size.height.toFloat() - padding.y.toPx() * 2,
                                )
                                .toPoint()
                        }
                    detectDragGestures(
                        onDragStart = { offset ->
                            val normalized = offset.normalize()
                            println("onDragStart $size $offset $normalized ${
                                normalized.distance(gradient.startPoint)
                            } ${
                                normalized.distance(gradient.endPoint)
                            }")
                            movingPoint = if (normalized.distance(gradient.startPoint)
                                < normalized.distance(gradient.endPoint)
                            ) 1 else 2
                        },
                        onDrag = { change, _ ->
                            val normalized = change.position.normalize()
                            gradient = when (movingPoint) {
                                1 -> gradient.copy(
                                    startPoint = normalized
                                )
                                2 -> gradient.copy(
                                    endPoint = normalized
                                )
                                else -> {
                                    throw IllegalStateException()
                                }
                            }
                        },
                        onDragEnd = {
                            println("gradient ${gradient.startPoint} ${gradient.endPoint}")
                        },
                    )
                }
        ) {
            fun Point.denormalize(): Offset =
                toOffset().denormalize(Size(constraints.minWidth.toFloat(), constraints.minHeight.toFloat()))
            GradientView(
                gradient,
                modifier = Modifier.matchParentSize()
            )
            Pointer(
                gradient
                    .startPoint
                    .denormalize(),
                gradient
                    .stops
                    .first()
                    .color.toColor(),
            )
            Pointer(
                gradient
                    .endPoint
                    .denormalize(),
                gradient
                    .stops
                    .last()
                    .color.toColor()
            )
        }
    }
}

private operator fun Point.plus(other: Point): Point =
    Point(x + other.x, y + other.y)

@Composable
private fun Pointer(offset: Offset, color: Color) {
    val size = 45.dp
    val visibleSize = 10.dp
    Box(
        modifier = Modifier
            .offset(offset - with(LocalDensity.current) {
                Offset(size.toPx() / 2,
                    size.toPx() / 2)
            })
            .size(size)
            .background(Color.Red.copy(alpha = 0.5f))
            .padding((size - visibleSize) / 2)
            .background(color.copy(alpha = 0.5f))
    )
}

fun Modifier.offset(offset: Offset) = composed {
    with(LocalDensity.current) {
        offset(offset.x.toDp(), offset.y.toDp())
    }
}

private fun Offset.normalize(width: Float, height: Float): Offset {
    return Offset(
        x / width,
        y / height
    )
}

private fun Point.distance(point: Point) = hypot(x - point.x, y - point.y)

private fun Offset.toPoint() = Point(x, y)