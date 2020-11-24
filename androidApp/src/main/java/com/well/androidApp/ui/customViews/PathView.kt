package com.well.androidApp.ui.customViews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.graphics.Path
import com.well.serverModels.Point
import com.well.serverModels.colorInt
import kotlin.math.absoluteValue
import kotlin.math.min

class PathView(context: Context) : View(context) {
    private val graphicsPath = Path()
    private val paint = Paint()

    companion object {
        private const val touchTolerance = 4f
    }

    var path: com.well.serverModels.Path? = null
        set(value) {
            if (value == field) {
                return
            }
            field = value
            graphicsPath.reset()
            invalidate()
            if (value == null) {
                return
            }
            paint.color = value.color.colorInt()

            var lastPoint = value.points[0]
            graphicsPath.moveTo(lastPoint)
            value.points.drop(1).forEachIndexed { i, it ->
                if (min(
                        (lastPoint.x - it.x).absoluteValue,
                        (lastPoint.y - it.y).absoluteValue
                    ) >= touchTolerance
                    && i < value.points.count() - 2
                ) {
                    graphicsPath.quadTo(
                        lastPoint.x,
                        lastPoint.y,
                        (it.x + lastPoint.x) / 2,
                        (it.y + lastPoint.y) / 2
                    )
                } else {
                    graphicsPath.lineTo(it)
                }
                lastPoint = it
            }
        }

    init {
        paint.isAntiAlias = true
        paint.isDither = true
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 6F
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(graphicsPath, paint)
    }

    private fun Path.lineTo(point: Point) {
        lineTo(point.x, point.y)
    }

    private fun Path.moveTo(point: Point) {
        moveTo(point.x, point.y)
    }
}
