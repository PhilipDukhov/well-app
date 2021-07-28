package com.well.modules.models

import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake

fun Size.toCGSize() =
    CGSizeMake(width.toDouble(), height.toDouble())

fun CGSize.toSize() = Size(width.toFloat(), height.toFloat())

