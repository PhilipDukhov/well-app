package com.well.modules.utils.viewUtils.platform

import okio.FileSystem
import platform.UIKit.UIScreen

actual val Platform.isDebug: Boolean
    get() = kotlin.native.Platform.isDebugBinary

actual val Platform.nativeScale: Float
    get() = UIScreen.mainScreen.nativeScale.toFloat()

actual val Platform.current: Platform.Platform
    get() = Platform.Platform.Ios

actual val Platform.fileSystem: FileSystem
    get() = FileSystem.SYSTEM