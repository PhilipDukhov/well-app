package com.well.androidApp.ui.composableScreens.chatList

import com.well.androidApp.ui.composableScreens.πCustomViews.Control
import com.well.androidApp.ui.composableScreens.πCustomViews.NavigationBar
import com.well.sharedMobile.puerh.chatList.ChatListFeature.Msg
import com.well.sharedMobile.puerh.chatList.ChatListFeature.State
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable

@Composable
fun ChatListScreen(
    state: State,
    listener: (Msg) -> Unit,
) {
    NavigationBar(
        title = "Chat list"
    )
    LazyColumn {
        items(state.listItems) { item ->
            Control(onClick = {
                listener(Msg.SelectChat(item.user.id))
            }) {
                ChatListCell(item = item)
            }
        }
    }
}