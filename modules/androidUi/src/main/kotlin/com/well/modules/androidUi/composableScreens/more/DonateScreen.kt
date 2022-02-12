package com.well.modules.androidUi.composableScreens.more

import com.well.modules.androidUi.customViews.ControlItem
import com.well.modules.androidUi.customViews.NavigationBar
import com.well.modules.features.more.moreFeature.subfeatures.DonateFeature.Msg
import com.well.modules.features.more.moreFeature.subfeatures.DonateFeature.State
import com.well.modules.features.more.moreFeature.subfeatures.DonateFeature as Feature
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable

@Composable
fun ColumnScope.DonateScreen(
    state: State,
    listener: (Msg) -> Unit,
) {
    NavigationBar(
        title = Feature.Strings.title,
        leftItem = ControlItem.back { listener(Msg.Back) }
    )

}