package com.well.utils.platform

expect val Platform.Companion.isDebug: Boolean
expect val Platform.Companion.nativeScale: Float

const val prodTesting = false