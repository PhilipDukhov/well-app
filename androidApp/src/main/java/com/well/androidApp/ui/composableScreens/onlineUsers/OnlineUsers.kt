package com.well.androidApp.ui.composableScreens.onlineUsers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.well.androidApp.ui.composableScreens.πCustomViews.UserProfileImage
import com.well.sharedMobile.puerh.onlineUsers.OnlineUsersFeature
import com.well.sharedMobile.puerh.onlineUsers.OnlineUsersFeature.Msg
import dev.chrisbanes.accompanist.insets.systemBarsPadding

@Composable
fun OnlineUsersScreen(
    state: OnlineUsersFeature.State,
    listener: (Msg) -> Unit,
) = Column(
    horizontalAlignment = CenterHorizontally,
    modifier = Modifier
        .systemBarsPadding()
        .fillMaxWidth()
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(30.dp)
    ) {
        Text(
            state.connectionStatus.stringRepresentation,
            modifier = Modifier
                .align(Alignment.Center)
        )
        state.currentUser?.let {
            UserProfileImage(
                it,
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.CenterEnd)
            )
        }
    }
    LazyColumn {
        items(state.users) { user ->
            UserCell(
                user,
                onClick = {
                    listener.invoke(Msg.OnUserSelected(user))
                }
            )
        }
    }
}
