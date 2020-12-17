package com.well.androidApp.ui.composableScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.well.androidApp.R
import com.well.serverModels.User
import com.well.serverModels.User.Type.Facebook
import com.well.serverModels.User.Type.Google
import com.well.shared.puerh.WebSocketManager.Status.*
import com.well.shared.puerh.onlineUsers.OnlineUsersFeature
import dev.chrisbanes.accompanist.insets.systemBarsPadding

@Composable
fun OnlineUsersScreen(
    state: OnlineUsersFeature.State,
    listener: (OnlineUsersFeature.Msg) -> Unit,
) = Column(
    horizontalAlignment = CenterHorizontally,
    modifier = Modifier
        .systemBarsPadding()
        .fillMaxWidth()
) {
    Text(
        when (state.connectionStatus) {
            Disconnected -> "Disconnected"
            Connecting -> "Connecting"
            Connected -> "Connected"
        },
        textAlign = TextAlign.Center,
    )
    LazyColumnFor(state.users) { user ->
        UserCell(
            user,
            onClick = {
                listener.invoke(OnlineUsersFeature.Msg.OnUserSelected(user))
            }
        )
    }
}

private val padding = 16.dp

@Composable
fun UserCell(user: User, onClick: () -> Unit) =
    Row(
        verticalAlignment = CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(padding)
    ) {
        Image(
            vectorResource(user.drawable),
            colorFilter = ColorFilter.tint(Color.Blue),
            modifier = Modifier
                .preferredHeight(30.dp)
        )
        Spacer(modifier = Modifier.width(padding))
        Text(
            text = user.fullName,
            maxLines = 1,
        )
    }

@Preview
@Composable
fun PreviewGreeting() {
    UserCell(
        User(
            1,
            "Phil",
            "Dukhov aksdnkjansdkj ansjkd naksj dnkjas ndkja nskjd naksjdn akjsnd kjans dkja",
            Google,
        )
    ) {}
}

val User.drawable
    get() = when (type) {
        Facebook -> R.drawable.ic_auth_facebook
        Google -> R.drawable.ic_auth_google
    }
