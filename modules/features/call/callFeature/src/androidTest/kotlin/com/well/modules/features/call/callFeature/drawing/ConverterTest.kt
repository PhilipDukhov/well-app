package com.well.modules.features.call.callFeature.drawing

import com.well.modules.models.Point
import com.well.modules.models.Size
import com.well.modules.features.call.callFeature.drawing.DrawingFeature.State.Converter
import kotlin.test.Test
import kotlin.test.assertEquals

class ConverterTest {
    @Test
    fun first() {
        val imageSize = Size(100, 100)
        val drawingConverter1 = Converter(
            containerSize = Size(100, 150),
            aspectRatio = imageSize,
            contentMode = Converter.ContentMode.AspectFit,
        )
        val drawingConverter2 = Converter(
            containerSize = Size(200, 250),
            aspectRatio = imageSize,
            contentMode = Converter.ContentMode.AspectFit,
        )

        listOf(
            Point(0f, 25f) to Point(0f, 25f),
            Point(100f, 25f) to Point(200f, 25f),
            Point(100f, 125f) to Point(200f, 225f),
            Point(50f, 75f) to Point(100f, 125f),
        ).forEach {
            val normalizedP = drawingConverter1.normalize(it.first)
            val denormalizedP = drawingConverter2.denormalize(normalizedP)
            assertEquals(it.second, denormalizedP, "${it.first} -> $normalizedP ->$denormalizedP")
        }
    }
}