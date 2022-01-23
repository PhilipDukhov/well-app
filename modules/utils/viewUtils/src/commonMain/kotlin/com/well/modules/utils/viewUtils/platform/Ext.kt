package com.well.modules.utils.viewUtils.platform

expect val Platform.Companion.isDebug: Boolean
expect val Platform.Companion.nativeScale: Float
expect val Platform.Companion.current: Platform.Platform

private const val prodTesting = true

val Platform.Companion.isLocalServer get() = !prodTesting && isDebug
