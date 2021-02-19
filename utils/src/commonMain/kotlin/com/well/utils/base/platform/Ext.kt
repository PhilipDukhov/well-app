package com.well.utils.platform

expect val Platform.Companion.isDebug: Boolean
expect val Platform.Companion.nativeScale: Float
expect val Platform.Companion.current: Platform.Platform

const val prodTesting = false