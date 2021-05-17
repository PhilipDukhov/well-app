package com.well.androidApp.ui.composableScreens.experts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import com.well.androidApp.ui.composableScreens.πCustomViews.ControlItem
import com.well.androidApp.ui.composableScreens.πCustomViews.NavigationBar
import com.well.androidApp.ui.composableScreens.πCustomViews.UserProfileImage
import com.well.sharedMobile.puerh.experts.ExpertsFeature
import com.well.sharedMobile.puerh.experts.ExpertsFeature.Msg
import com.google.accompanist.insets.navigationBarsPadding

@Composable
fun ExpertsScreen(
    state: ExpertsFeature.State,
    listener: (Msg) -> Unit,
) = Column(
    horizontalAlignment = CenterHorizontally,
    modifier = Modifier
        .fillMaxSize()
) {
    NavigationBar(
        title = state.connectionStatus.stringRepresentation,
        leftItem = ControlItem(text = "Log out") {
            listener.invoke(Msg.OnLogout)
        },
        rightItem = ControlItem(
            view = { UserProfileImage(state.currentUser) },
            handler = { listener.invoke(Msg.OnCurrentUserSelected) },
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
                onCall = {
//                    listener.invoke(Msg.OnCallUser(user))
                },
            )
            Divider()
        }
    }
}
