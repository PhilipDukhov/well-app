package com.well.androidApp.ui.composableScreens.experts

import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.experts.filter.FilterScreen
import com.well.androidApp.ui.customViews.ControlItem
import com.well.androidApp.ui.customViews.NavigationBar
import com.well.androidApp.ui.ext.toColor
import com.well.modules.models.Color
import com.well.sharedMobile.puerh.experts.ExpertsFeature
import com.well.sharedMobile.puerh.experts.ExpertsFeature.Msg
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.google.accompanist.insets.navigationBarsPadding

@Composable
fun ColumnScope.ExpertsScreen(
    state: ExpertsFeature.State,
    listener: (Msg) -> Unit,
) {
    var filterScreenVisible by remember { mutableStateOf(false) }
    if (!filterScreenVisible) {
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
    state: ExpertsFeature.State,
    listener: (Msg) -> Unit,
    showFilterScreen: () -> Unit,
) {
    NavigationBar(
        title = state.connectionStatus.stringRepresentation,
        leftItem = ControlItem(
            view = {
                Row {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "",
                        tint = Color.White.toColor(),
                    )
                    Text(
                        "Filter",
                        style = MaterialTheme.typography.subtitle2,
                        color = Color.White.toColor(),
                    )
                }
            },
            handler = showFilterScreen,
        ),
        rightItem = ControlItem(
            view = {
                Icon(
                    painterResource(
                        if (state.filterState.filter.favorite)
                            R.drawable.ic_round_favorite_24
                        else
                            R.drawable.ic_round_favorite_border_24
                    ),
                    contentDescription = "",
                    tint = Color.White.toColor(),
                )
            },
            handler = { listener.invoke(Msg.ToggleFilterFavorite) },
        ),
    )
    LazyColumn(modifier = Modifier.navigationBarsPadding()) {
        if (state.users.isNotEmpty()) {
            item { Divider() }
        }
        items(state.users) { user ->
            UserCell(
                user,
                onSelect = {
                    listener.invoke(Msg.OnUserSelected(user))
                },
                onToggleFavorite = {
                    listener.invoke(Msg.OnUserFavorite(user))
                },
            )
            Divider()
        }
    }
}
