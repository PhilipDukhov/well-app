package com.well.serverModels

import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake

fun Size.toCGSize() =
    CGSizeMake(width.toDouble(), height.toDouble())

fun CValue<CGSize>.toSize() =
    useContents {
        Size(width.toFloat(), height.toFloat())
    }

