package com.well.androidAppTest

import com.well.modules.androidUi.composableScreens.userChat.UserChatScreen
import com.well.modules.features.userChat.userChatFeature.UserChatFeature
import com.well.modules.models.User
import com.well.modules.models.chat.ChatMessage
import com.well.modules.models.chat.ChatMessageViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
internal fun UserChatTest() {
    val state = remember {
        mutableStateOf(
            UserChatFeature.State(
                peerId = User.Id(0), user = null, backToUser = false,
                messages = List(100) {
                    ChatMessageViewModel(
                        id = ChatMessage.Id(it.toLong()),
                        creation = 0.0,
                        content = ChatMessage.Content.Text("akjsdnasjk"),
                        status = ChatMessageViewModel.Status.IncomingRead,
                    )
                }
            )
        )
    }
    Column {
        UserChatScreen(
            state = state.value,
            listener = {
                println("msg $it")
                state.value = UserChatFeature.reducer(it, state.value).first
            })
    }
}