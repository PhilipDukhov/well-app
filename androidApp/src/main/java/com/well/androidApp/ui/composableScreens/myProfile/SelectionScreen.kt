package com.well.androidApp.ui.composableScreens.myProfile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.well.androidApp.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.well.androidApp.ui.composableScreens.πExt.Image
import com.well.androidApp.ui.composableScreens.πCustomViews.ControlItem
import com.well.androidApp.ui.composableScreens.πCustomViews.NavigationBar
import com.well.androidApp.ui.composableScreens.πExt.backgroundKMM
import com.well.androidApp.ui.composableScreens.πExt.toColor
import com.well.serverModels.Color
import dev.chrisbanes.accompanist.insets.navigationBarsPadding

@Composable
fun SelectionScreen(
    title: String,
    selection: Set<String>,
    variants: List<String>,
    multipleSelection: Boolean,
    onSelectionChanged: (Set<String>) -> Unit,
    onCancel: () -> Unit,
){
    var selectionState by remember { mutableStateOf(selection) }
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        NavigationBar(
            title = title,
            leftItem = ControlItem(text = "Cancel") {
                onCancel()
            },
            rightItem = if (multipleSelection) ControlItem(text = "Done") {
                onSelectionChanged(selectionState)
            } else null,
        )
        LazyColumn(modifier = Modifier.navigationBarsPadding()) {
            items(variants) { variant ->
                val selected = selectionState.contains(variant)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .backgroundKMM(if (selected) Color.Green10 else Color.Transparent)
                        .clickable {
                            if (multipleSelection) {
                                selectionState = selectionState.toMutableSet().apply {
                                    if (selected) {
                                        remove(variant)
                                    } else {
                                        add(variant)
                                    }
                                }
                            } else {
                                onSelectionChanged(setOf(variant))
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
}