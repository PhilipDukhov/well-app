package com.well.modules.features.more

import com.well.modules.features.more.about.AboutFeature
import com.well.modules.features.more.support.SupportFeature

sealed class ScreenState {
    data class Support(val state: SupportFeature.State): ScreenState()
    data class About(val state: AboutFeature.State): ScreenState()
}