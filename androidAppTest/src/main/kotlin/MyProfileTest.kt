@file:Suppress("UNCHECKED_CAST")

package com.well.androidAppTest

import com.well.androidAppTest.Utility.LocalSystemHelper
import com.well.modules.androidUi.composableScreens.myProfile.MyProfileScreen
import com.well.modules.androidUi.customViews.rememberPreference
import com.well.sharedMobileTest.MyProfileTestModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.google.accompanist.insets.navigationBarsPadding

@Composable
internal fun MyProfileTest() {
    var isCurrent by rememberPreference(booleanPreferencesKey("MyProfileTest_isCurrent"), false)
    val appContext = LocalSystemHelper.current
    val viewModel = remember(isCurrent, appContext) {
        MyProfileTestModel(isCurrent, appContext)
    }
    Box {
        Column {
            MyProfileScreen(
                state = viewModel.state.collectAsState().value,
                listener = viewModel::listener
            )
        }

        Switch(
            checked = isCurrent,
            onCheckedChange = { isCurrent = !isCurrent },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
        )
    }
}