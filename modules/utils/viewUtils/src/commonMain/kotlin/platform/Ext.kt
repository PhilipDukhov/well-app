package com.well.modules.utils.viewUtils.platform

import okio.FileSystem
import okio.Path

expect val Platform.isDebug: Boolean
expect val Platform.nativeScale: Float
expect val Platform.current: Platform.Platform
expect val Platform.fileSystem: FileSystem

val Platform.isLocalServer get() = !prodTesting && isDebug

private const val prodTesting = false

