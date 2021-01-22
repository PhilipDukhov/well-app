package com.well.sharedMobile.puerh.call.imageSharing

import com.well.serverModels.Point
import com.well.serverModels.Size
import com.well.sharedMobile.puerh.call.imageSharing.ImageSharingFeature.State.DrawingConverter
import kotlin.test.Test
import kotlin.test.assertEquals

class DrawingConverterTest {
    @Test
    fun first() {
        val imageSize = Size(100, 100)
        val drawingConverter1 = DrawingConverter(
            containerSize = Size(100, 150),
            imageSize = imageSize
        )
        val drawingConverter2 = DrawingConverter(
            containerSize = Size(200, 250),
            imageSize = imageSize
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