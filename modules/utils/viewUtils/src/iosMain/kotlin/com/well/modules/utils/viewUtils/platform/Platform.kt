package com.well.modules.utils.viewUtils.platform

import platform.UIKit.UIScreen

actual val Platform.Companion.isDebug: Boolean
    get() = kotlin.native.Platform.isDebugBinary

actual val Platform.Companion.nativeScale: Float
    get() = UIScreen.mainScreen.nativeScale.toFloat()

actual val Platform.Companion.current: Platform.Platform
    get() = Platform.Platform.Ios