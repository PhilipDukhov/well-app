package com.well.androidAppTest

import androidx.compose.runtime.Composable
import com.well.modules.androidUi.composableScreens.userChat.UserChatScreen
import com.well.modules.features.userChat.userChatFeature.UserChatFeature
import com.well.modules.models.chat.ChatMessage
import com.well.modules.viewHelpers.chatMessageWithStatus.ChatMessageWithStatus
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
internal fun UserChatTest() {
    val state = remember {
        mutableStateOf(
            UserChatFeature.State(
                peerId = 0, user = null, backToUser = false,
                messages = listOf(
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                    ChatMessageWithStatus(
                        message = ChatMessage(
                            id = 0,
                            creation = 0.0,
                            fromId = 0,
                            peerId = 0,
                            content = ChatMessage.Content.Text("akjsdnasjk"),
                        ), status = ChatMessageWithStatus.Status.IncomingRead
                    ),
                )
            )
        )
    }
    Column {
        UserChatScreen(
            state = state.value,
            listener = {
                println("msg $it")
//                state.value = UserChatFeature.reducer(it, state.value).first
            })
    }
}