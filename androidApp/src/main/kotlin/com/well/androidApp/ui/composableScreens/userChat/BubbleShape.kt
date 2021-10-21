package com.well.androidApp.ui.composableScreens.userChat

import com.well.androidApp.ui.ext.toPx
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class BubbleShape: Shape {
    companion object {
        private const val dxPart = 0.34f

        val radius = 17f.dp
        val tailDx = radius * dxPart
    }

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) = Outline.Generic(Path().apply {
        fillType = PathFillType.NonZero
        val radius = radius.toPx(density)
        val diameter = radius * 2
        val rect = RoundRect(
            left = 0f,
            top = 0f,
            right = size.width - radius * dxPart,
            bottom = size.height,
            cornerRadius = CornerRadius(radius),
        )
        moveTo(x = radius, y = rect.top)
        lineTo(x = rect.width - radius, y = rect.top)
        arcTo(
            rect = Rect(
                left = rect.right - diameter,
                top = rect.top,
                right = rect.right,
                bottom = diameter,
            ),
            startAngleDegrees = 270f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false,
        )
        val startX = rect.width
        val startY = size.height - radius
        lineTo(x = startX, y = startY)
        val dyPart = 0.48f
        relativeLineTo(dx = 0f, dy = (1 - dyPart) * radius)
        relativeCubicTo(
            multiplier = radius,
            dx1 = 0.0f,
            dy1 = 0.21789444f,
            dx2 = 0.14563929f,
            dy2 = 0.40361276f,
            dx3 = dxPart,
            dy3 = dyPart,
        )
        relativeCubicTo(
            multiplier = radius,
            dx1 = -0.25966695f,
            dy1 = -0.00169348f,
            dx2 = -0.51312447f,
            dy2 = -0.07056167f,
            dx3 = -0.72424499f,
            dy3 = -0.19023426f,
        )
        val degree = 45.97143714203f
        arcTo(
            rect = Rect(
                left = rect.right - diameter,
                top = rect.bottom - diameter,
                right = rect.right,
                bottom = rect.bottom,
            ),
            startAngleDegrees = 90f - degree,
            sweepAngleDegrees = degree,
            forceMoveTo = true,
        )
        arcTo(
            rect = Rect(
                left = rect.left,
                top = rect.bottom - diameter,
                right = rect.left + diameter,
                bottom = rect.bottom,
            ),
            startAngleDegrees = 90f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false,
        )
        arcTo(
            rect = Rect(
                left = rect.left,
                top = rect.top,
                right = rect.left + diameter,
                bottom = rect.top + diameter,
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false,
        )
        close()
    })

    private fun Path.relativeCubicTo(multiplier: Float, dx1: Float, dy1: Float, dx2: Float, dy2: Float, dx3: Float, dy3: Float) =
        relativeCubicTo(
            dx1 = dx1 * multiplier,
            dy1 = dy1 * multiplier,
            dx2 = dx2 * multiplier,
            dy2 = dy2 * multiplier,
            dx3 = dx3 * multiplier,
            dy3 = dy3 * multiplier,
        )
}