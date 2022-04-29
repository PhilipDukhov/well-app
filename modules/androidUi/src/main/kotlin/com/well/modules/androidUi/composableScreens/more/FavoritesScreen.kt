package com.well.modules.androidUi.composableScreens.more

import com.well.modules.androidUi.components.ControlItem
import com.well.modules.androidUi.components.NavigationBar
import com.well.modules.androidUi.components.usersList.UsersList
import com.well.modules.features.more.moreFeature.subfeatures.FavoritesFeature.Msg
import com.well.modules.features.more.moreFeature.subfeatures.FavoritesFeature.State
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color

@Composable
fun ColumnScope.FavoritesScreen(
    state: State,
    listener: (Msg) -> Unit,
) {
    val filterString = state.filterString
    NavigationBar(
        title = if (filterString == null) ControlItem(text = state.title) else null,
        leftItem = if (filterString == null) ControlItem.back { listener(Msg.Back) } else null,
        rightItem = if (filterString == null) {
            ControlItem(
                handler = {
                    listener(Msg.UpdateFilterString(""))
                },
                view = {
                    Icon(Icons.Default.Search, null)
                }
            )
        } else {
            ControlItem(
                view = {
                    val focusRequester = remember { FocusRequester() }
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                    TextField(
                        value = filterString,
                        onValueChange = {
                            listener(Msg.UpdateFilterString(it))
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, null)
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                listener(Msg.UpdateFilterString(null))
                            }) {
                                Icon(Icons.Default.Clear, null)
                            }
                        },
                        placeholder = {
                            Text("Search")
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.White.copy(alpha = 0.2f),
                            trailingIconColor = Color.White,
                            leadingIconColor = Color.White,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                }
            )
        }
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