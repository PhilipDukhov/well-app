package com.well.modules.androidUi.composableScreens.chatList

import com.well.modules.androidUi.customViews.Control
import com.well.modules.androidUi.customViews.NavigationBar
import com.well.modules.features.chatList.chatListFeature.ChatListFeature.Msg
import com.well.modules.features.chatList.chatListFeature.ChatListFeature.State
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