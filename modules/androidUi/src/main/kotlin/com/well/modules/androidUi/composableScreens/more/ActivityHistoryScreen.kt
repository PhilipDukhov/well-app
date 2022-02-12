package com.well.modules.androidUi.composableScreens.more

import com.well.modules.androidUi.customViews.ControlItem
import com.well.modules.androidUi.customViews.NavigationBar
import com.well.modules.features.more.moreFeature.subfeatures.ActivityHistoryFeature.Msg
import com.well.modules.features.more.moreFeature.subfeatures.ActivityHistoryFeature.State
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable

@Composable
fun ColumnScope.ActivityHistoryScreen(
    state: State,
    listener: (Msg) -> Unit,
) {
    NavigationBar(
        title = state.title,
        leftItem = ControlItem.back { listener(Msg.Back) }
    )

}