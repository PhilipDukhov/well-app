package com.well.modules.androidUi.composableScreens.experts

import com.well.modules.androidUi.composableScreens.experts.filter.FilterScreen
import com.well.modules.androidUi.customViews.NavigationBar
import com.well.modules.androidUi.customViews.rememberControlItem
import com.well.modules.androidUi.customViews.usersList.UsersList
import com.well.modules.androidUi.ext.toColor
import com.well.modules.features.experts.expertsFeature.ExpertsFeature.Msg
import com.well.modules.models.Color
import com.well.modules.features.experts.expertsFeature.ExpertsFeature as Feature
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun ColumnScope.ExpertsScreen(
    state: Feature.State,
    listener: (Msg) -> Unit,
) {
    var filterScreenVisible by remember { mutableStateOf(false) }
    if (!filterScreenVisible) {
        BackHandler {
            filterScreenVisible = false
        }
        ExpertsScreenContent(
            state,
            listener,
            showFilterScreen = {
                filterScreenVisible = true
            })
    } else {
        FilterScreen(
            state = state.filterState,
            listener = {
                listener(Msg.FilterMsg(it))
            },
            hide = {
                filterScreenVisible = false

            }
        )
    }
}

@Composable
private fun ExpertsScreenContent(
    state: Feature.State,
    listener: (Msg) -> Unit,
    showFilterScreen: () -> Unit,
) {
    NavigationBar(
        title = state.connectionStatusDescription,
        leftItem = rememberControlItem(handler = showFilterScreen) {
            Row {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = null,
                    tint = Color.White.toColor(),
                )
                Text(
                    Feature.Strings.filter,
                    style = MaterialTheme.typography.subtitle2,
                    color = Color.White.toColor(),
                )
            }
        },
        rightItem = rememberControlItem(
            state.filterState.filter.favorite,
            handler = { listener.invoke(Msg.ToggleFilterFavorite) },
        ) {
            Icon(
                with(Icons.Rounded) {
                    if (state.filterState.filter.favorite) Favorite else FavoriteBorder
                },
                contentDescription = null,
                tint = Color.White.toColor(),
            )
        },
    )
    UsersList(
        users = state.users,
        onSelect = {
            listener(Msg.OnUserSelected(it))
        },
        onToggleFavorite = {
            listener(Msg.OnUserFavorite(it))
        },
        modifier = Modifier
            .navigationBarsPadding()
    )
}