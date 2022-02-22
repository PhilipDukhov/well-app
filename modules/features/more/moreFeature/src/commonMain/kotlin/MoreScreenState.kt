package com.well.modules.features.more.moreFeature

import com.well.modules.features.more.moreFeature.subfeatures.FavoritesFeature
import com.well.modules.features.more.moreFeature.subfeatures.AboutFeature
import com.well.modules.features.more.moreFeature.subfeatures.ActivityHistoryFeature
import com.well.modules.features.more.moreFeature.subfeatures.DonateFeature
import com.well.modules.features.more.moreFeature.subfeatures.SupportFeature
import com.well.modules.features.more.moreFeature.subfeatures.WellAcademyFeature

sealed interface MoreScreenState {
    data class Favorites(val state: FavoritesFeature.State): MoreScreenState
    data class WellAcademy(val state: WellAcademyFeature.State): MoreScreenState
    data class ActivityHistory(val state: ActivityHistoryFeature.State): MoreScreenState
    data class Donate(val state: DonateFeature.State): MoreScreenState
    data class Support(val state: SupportFeature.State): MoreScreenState
    data class About(val state: AboutFeature.State): MoreScreenState
}