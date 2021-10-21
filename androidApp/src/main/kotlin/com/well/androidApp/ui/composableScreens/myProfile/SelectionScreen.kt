package com.well.androidApp.ui.composableScreens.myProfile

import com.well.androidApp.R
import com.well.androidApp.ui.ext.Image
import com.well.androidApp.ui.ext.backgroundKMM
import com.well.androidApp.ui.ext.toColor
import com.well.modules.models.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsPadding

@Composable
fun SelectionScreen(
    selectionState: MutableState<Set<Int>>,
    variants: List<String>,
    multipleSelection: Boolean,
    onFinish: (Set<Int>) -> Unit,
) {
    var selection by selectionState
    LazyColumn(modifier = Modifier.navigationBarsPadding()) {
        itemsIndexed(variants) { index, variant ->
            val selected = selection.contains(index)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .backgroundKMM(if (selected) Color.Green10 else Color.Transparent)
                    .clickable {
                        if (multipleSelection) {
                            selection = selection
                                .toMutableSet()
                                .apply {
                                    if (selected) {
                                        remove(index)
                                    } else {
                                        add(index)
                                    }
                                }
                        } else {
                            onFinish(setOf(index))
                        }
                    }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .fillMaxSize()
            ) {
                Text(variant)
                if (selected) {
                    Image(
                        painterResource(R.drawable.ic_baseline_check_24),
                        colorFilter = ColorFilter.tint(Color.Green.toColor())
                    )
                }
            }
            Divider()
        }
    }
}