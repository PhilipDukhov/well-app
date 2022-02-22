package com.well.modules.androidUi.composableScreens.call.drawing

import com.well.modules.androidUi.ext.toColor
import com.well.modules.features.call.callFeature.drawing.DrawingFeature.Msg
import com.well.modules.features.call.callFeature.drawing.DrawingFeature.State
import com.well.modules.models.Point
import com.well.modules.models.Size
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import kotlin.math.hypot

@Composable
fun DrawingContent(
    state: State,
    listener: (Msg) -> Unit,
    enabled: Boolean = true,
    onSizeChanged: ((Size) -> Unit)? = null,
    modifier: Modifier,
) {
    val filter = if (enabled) Modifier.pointerInteropFilter {
        when (it.action) {
            MotionEvent.ACTION_MOVE -> {
                listener(Msg.NewDragPoint(Point(it.x, it.y)))
            }
            MotionEvent.ACTION_UP -> {
                listener(Msg.EndDrag)
            }
            else -> Unit
        }
        true
    } else Modifier
    Box(
        modifier = modifier
            .then(filter)
            .fillMaxSize()
            .onSizeChanged {
                onSizeChanged?.invoke(Size(it.width, it.height))
            }) {
        state.canvasPaths.forEach { path ->
            PathCanvas(
                path = path,
                cap = state.nativeStrokeStyle.lineCap.toStrokeCap(),
                join = state.nativeStrokeStyle.lineJoin.toStrokeJoin(),
            )
        }
    }
}

@Composable
private fun PathCanvas(
    path: com.well.modules.models.DrawingPath,
    cap: StrokeCap,
    join: StrokeJoin,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawPath(
            Path().apply {
                val points = path.points
                if (points.count() < 2) {
                    return@apply
                }
                moveTo(points[0].x, points[0].y)
                var lastPoint = points[0]
                val touchTolerance = path.lineWidth * density
                var quadraticCount = 0
                points.drop(1).forEachIndexed { i, point ->
                    if (
                        i < points.lastIndex - 1 &&
                        hypot(lastPoint.x - point.x, lastPoint.y - point.y) >= touchTolerance
                    ) {
                        quadraticBezierTo(
                            lastPoint.x,
                            lastPoint.y,
                            (point.x + lastPoint.x) / 2,
                            (point.y + lastPoint.y) / 2
                        )
                        quadraticCount += 1
                    } else {
                        lineTo(point.x, point.y)
                    }
                    lastPoint = point
                }
            },
            brush = SolidColor(path.color.toColor()),
            style = Stroke(
                width = path.lineWidth * density,
                cap = cap,
                join = join,
            )
        )
    }
}

private fun State.StrokeStyle.LineCap.toStrokeCap(): StrokeCap =
    when (this) {
        State.StrokeStyle.LineCap.Butt -> StrokeCap.Butt
        State.StrokeStyle.LineCap.Round -> StrokeCap.Round
        State.StrokeStyle.LineCap.Square -> StrokeCap.Square
    }

private fun State.StrokeStyle.LineJoin.toStrokeJoin(): StrokeJoin =
    when (this) {
        State.StrokeStyle.LineJoin.Miter -> StrokeJoin.Miter
        State.StrokeStyle.LineJoin.Round -> StrokeJoin.Round
        State.StrokeStyle.LineJoin.Bevel -> StrokeJoin.Bevel
    }