package com.well.sharedMobile.puerh.call

import com.well.serverModels.Point
import com.well.serverModels.Size
import kotlin.random.Random
import kotlin.test.assertEquals

fun assertByteArrayEquals(
    expected: ByteArray,
    actual: ByteArray,
    message: String? = null
) {
    assertEquals(
        expected.count(),
        actual.count(),
        "$message different count"
    )
    expected
        .zip(actual)
        .withIndex()
        .forEach {
            assertEquals(
                it.value.first,
                it.value.second,
                "$message different bytes ${it.index}"
            )
        }
}

fun ByteArray.toHexString() = asUByteArray()
    .joinToString("") {
        it.toString(16)
            .padStart(2, '0')
    }

fun Random.nextFloat(from: Float, until: Float) =
    Random.nextDouble(from.toDouble(), until.toDouble()).toFloat()

fun Random.nextPoint(size: Size) =
    Point(
        nextFloat(from = 0F, until = size.width),
        nextFloat(from = 0F, until = size.height),
    )