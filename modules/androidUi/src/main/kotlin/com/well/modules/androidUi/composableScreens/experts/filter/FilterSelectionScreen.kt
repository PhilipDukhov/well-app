package com.well.modules.androidUi.composableScreens.experts.filter

import com.well.modules.androidUi.composableScreens.myProfile.SelectionScreen
import com.well.modules.androidUi.customViews.ControlItem
import com.well.modules.androidUi.customViews.NavigationBar
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun FilterSelectionScreen(
    title: String,
    initialSelection: Set<Int>,
    variants: List<String>,
    multipleSelection: Boolean,
    onFinish: (Set<Int>) -> Unit,
) {
    val selectionState = remember { mutableStateOf(initialSelection) }
    var selection by selectionState
    BackHandler {
        onFinish(selection)
    }
    NavigationBar(
        title = title,
        rightItem = if (!multipleSelection) null else {
            val allVariants = variants.indices.toSet()
            val allVariantsSelected = selection == allVariants
            ControlItem(
                text = if (allVariantsSelected) "Deselect all" else "Select all",
                handler = {
                    selection = if (allVariantsSelected) setOf() else allVariants
                },
            )
        }
    )
    SelectionScreen(
        selectionState = selectionState,
        variants = variants,
        multipleSelection = multipleSelection,
        onFinish = onFinish
    )
}