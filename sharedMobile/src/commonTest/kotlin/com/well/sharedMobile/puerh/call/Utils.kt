package com.well.sharedMobile.puerh.call

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