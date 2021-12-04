package com.well.modules.utils.viewUtils.platform

import com.well.modules.models.Size
import com.well.modules.utils.viewUtils.BuildConfig

actual val Platform.Companion.isDebug: Boolean
    get() = BuildConfig.DEBUG

actual val Platform.Companion.nativeScale: Float
    get() = 1F

actual val Platform.Companion.current: Platform.Platform
    get() = Platform.Platform.Android