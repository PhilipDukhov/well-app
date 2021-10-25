package com.well.modules.androidUi.composableScreens.experts.filter

import com.well.modules.androidUi.theme.body1Light
import com.well.modules.androidUi.customViews.ActionButton
import com.well.modules.androidUi.customViews.ActionButtonStyle
import com.well.modules.androidUi.customViews.Control
import com.well.modules.androidUi.customViews.ControlItem
import com.well.modules.androidUi.customViews.NavigationBar
import com.well.modules.androidUi.customViews.toNavigationTitleText
import com.well.modules.androidUi.ext.backgroundKMM
import com.well.modules.androidUi.ext.borderKMM
import com.well.modules.androidUi.ext.toColor
import com.well.modules.models.Color
import com.well.modules.models.UsersFilter
import com.well.modules.features.experts.expertsFeature.filter.FilterFeature.Msg
import com.well.modules.features.experts.expertsFeature.filter.FilterFeature.State
import com.well.modules.utils.viewUtils.UIEditingField
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow

private typealias EditingField = UIEditingField<UIEditingField.Content.List<*>, Msg.Update>

@Composable
fun ColumnScope.FilterScreen(
    state: State,
    listener: (Msg) -> Unit,
    hide: () -> Unit,
) {
    val selectedFieldState = remember { mutableStateOf<EditingField?>(null) }
    val selectedField = selectedFieldState.value
    if (selectedField == null) {
        FilterScreenContent(
            state = state,
            listener = listener,
            hide = hide,
            selectField = {
                selectedFieldState.value = it
            },
        )
    } else {
        FilterSelectionScreen(
            title = selectedField.placeholder,
            initialSelection = selectedField.content.selectionIndices,
            variants = selectedField.content.itemDescriptions,
            multipleSelection = selectedField.content.multipleSelectionAvailable,
            onFinish = { newSelection ->
                listener(selectedField.updateMsg(selectedField.content.doCopy(selectionIndices = newSelection)))
                selectedFieldState.value = null
            }
        )
    }
}

@Composable
private fun FilterScreenContent(
    state: State,
    listener: (Msg) -> Unit,
    hide: () -> Unit,
    selectField: (EditingField) -> Unit
) {
    NavigationBar(
        title = ControlItem(
            view = {
                Row {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        tint = Color.White.toColor(),
                    )
                    "Filter".toNavigationTitleText()
                }
            }
        ),
    )
    LazyColumn {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    "Sort by",
                    style = MaterialTheme.typography.body1Light,
                )
                Spacer(modifier = Modifier.size(10.dp))
                SelectableRowColumnGrid(
                    items = UsersFilter.SortBy.allCases,
                    selectedIndex = state.sortByIndex,
                    selectIndex = {
                        listener(Msg.SetSortByIndex(it))
                    },
                    itemBuilder = { sortBy, selected ->
                        Text(
                            sortBy.name,
                            style = MaterialTheme.typography.body1Light,
                            color = (if (selected) Color.White else Color.Black).toColor(),
                            modifier = Modifier
                                .selectableRowColumnItemBackground(selected = selected)
                                .padding(vertical = 5.dp, horizontal = 10.dp)
                        )
                    }
                )
            }
        }
        item {
            Divider()
        }
        items(state.fields) { field ->
            Control(onClick = {
                selectField(field)
            }, content = {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .fillMaxSize()
                ) {
                    Text(field.placeholder)
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.LightGray.toColor()
                    )
                }
            })
            Divider()
        }
        item {
            Column(
                Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    "Rating",
                    style = MaterialTheme.typography.body1
                )
                SelectableRowColumnGrid(
                    items = UsersFilter.Rating.allCases,
                    selectedIndex = state.ratingIndex,
                    selectIndex = {
                        listener(Msg.SetRatingIndex(it))
                    },
                    itemBuilder = { rating, selected ->
                        Row(
                            Modifier
                                .selectableRowColumnItemBackground(selected = selected)
                                .padding(vertical = 5.dp, horizontal = 10.dp)
                        ) {
                            Text(
                                rating.title,
                                style = MaterialTheme.typography.body1Light,
                                color = (if (selected) Color.White else Color.Black).toColor(),
                            )
                            if (rating != UsersFilter.Rating.All) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = (if (selected) Color.White else Color.LightGray).toColor()
                                )
                            }
                        }
                    }
                )
            }
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .fillMaxWidth()
            ) {
                Text("With reviews")
                Switch(
                    checked = state.filter.withReviews,
                    onCheckedChange = {
                        listener(Msg.ToggleWithReviews)
                    }
                )
            }
        }
        item {
            ActionButton(
                style = ActionButtonStyle.White,
                onClick = {
                    listener(Msg.Clear)
                }) {
                Text("clear all")
            }
        }
        item {
            ActionButton(onClick = hide) {
                Text("Show")
            }
        }
    }
}

@Composable
fun <Item> SelectableRowColumnGrid(
    items: Collection<Item>,
    selectedIndex: Int,
    selectIndex: (Int) -> Unit,
    itemBuilder: @Composable (Item, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    spacing: Dp = 9.dp,
) {
    FlowRow(
        modifier = modifier,
        mainAxisSpacing = spacing,
        crossAxisSpacing = spacing,
    ) {
        items.forEachIndexed { index, item ->
            Control(
                onClick = {
                    if (selectedIndex != index) {
                        selectIndex(index)
                    }
                }, content = {
                    itemBuilder(item, index == selectedIndex)
                }
            )
        }
    }
}

@SuppressLint("ComposableModifierFactory")
@Composable
private fun Modifier.selectableRowColumnItemBackground(selected: Boolean) =
    RoundedCornerShape(100).let { shape ->
        borderKMM(
            width = (if (selected) 0.0 else 1.5).dp,
            color = Color.LightGray,
            shape = shape
        )
            .backgroundKMM(
                if (selected) Color.Green else Color.Transparent,
                shape = shape
            )
    }