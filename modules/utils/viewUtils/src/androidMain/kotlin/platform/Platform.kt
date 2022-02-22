package com.well.modules.utils.viewUtils.platform

import com.well.modules.utils.viewUtils.BuildConfig
import okio.FileSystem

actual val Platform.isDebug
    get() = BuildConfig.DEBUG

actual val Platform.nativeScale
    get() = 1F

actual val Platform.current
    get() = Platform.Platform.Android

actual val Platform.fileSystem
    get() = FileSystem.SYSTEM