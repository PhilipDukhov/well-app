package com.well.androidApp.ui.composableScreens.userChat

import com.well.androidApp.R
import com.well.androidApp.ui.customViews.Control
import com.well.androidApp.ui.customViews.ControlItem
import com.well.androidApp.ui.customViews.NavigationBar
import com.well.androidApp.ui.customViews.ProfileImage
import com.well.androidApp.ui.ext.toColor
import com.well.modules.models.Color
import com.well.modules.models.User
import com.well.modules.models.chat.ChatMessage
import com.well.sharedMobile.puerh.userChat.UserChatFeature.Msg
import com.well.sharedMobile.puerh.userChat.UserChatFeature.State
import com.well.sharedMobile.puerh.πModels.chatMessageWithStatus.ChatMessageWithStatus
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsWithImePadding
import kotlinx.coroutines.launch

@Composable
fun ColumnScope.UserChatScreen(
    state: State,
    listener: (Msg) -> Unit,
) {
    NavigationBar(
        leftItem = state.user?.let { user ->
            ControlItem(
                handler = {
                    listener(Msg.OpenUserProfile)
                }, view = {
                    UserInfo(user)
                }
            )
        },
        rightItem = ControlItem(
            handler = {
                listener(Msg.Call)
            }, view = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_call_24),
                    contentDescription = "",
                    tint = Color.White.toColor(),
                )
            }
        ),
    )

    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    Messages(
        messages = state.messages,
        markRead = { message ->
            listener(Msg.MarkMessageRead(message))
        },
        scrollState = scrollState,
        modifier = Modifier.weight(1f)
    )
    UserInput(
        selectPhoto = {
            listener(Msg.ChooseImage)
        },
        send = { text ->
            listener(Msg.SendMessage(text))
        },
        resetScroll = {
            scope.launch {
                scrollState.scrollToItem(0)
            }
        },
        modifier = Modifier
            .navigationBarsWithImePadding()
    )
}

@Composable
private fun UserInfo(
    user: User,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ProfileImage(
            user = user,
            modifier = Modifier
                .padding(10.dp)
                .size(40.dp)
        )
        Text(
            user.fullName,
            style = MaterialTheme.typography.subtitle2,
            color = Color.White.toColor(),
        )
    }
}

@Composable
private fun Messages(
    messages: List<ChatMessageWithStatus>,
    markRead: (ChatMessage) -> Unit,
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    Box(modifier = modifier) {
        LazyColumn(
            reverseLayout = true,
            state = scrollState,
            contentPadding = PaddingValues(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(messages) { message ->
                UserChatMessageCell(message)
            }
        }
        // Jump to bottom button shows up when user scrolls past a threshold.
        // Convert to pixels:
        val jumpThreshold = with(LocalDensity.current) {
            JumpToBottomThreshold.toPx()
        }

        // Show the button if the first visible item is not the first one or if the offset is
        // greater than the threshold.
        val jumpToBottomButtonEnabled by remember {
            derivedStateOf {
                scrollState.firstVisibleItemIndex != 0 ||
                        scrollState.firstVisibleItemScrollOffset > jumpThreshold
            }
        }

        JumpToBottom(
            // Only show if the scroller is not at the bottom
            enabled = jumpToBottomButtonEnabled,
            onClicked = {
                scope.launch {
                    scrollState.animateScrollToItem(0)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    val visibleIndices by remember {
        derivedStateOf {
            scrollState.layoutInfo.visibleItemsInfo.map { itemInfo ->
                itemInfo.index
            }
        }
    }
    visibleIndices.forEach { index ->
        val message = messages[index]
        if (message.status == ChatMessageWithStatus.Status.IncomingUnread) {
            markRead(message.message)
        }
    }
}

@Composable
fun UserInput(
    selectPhoto: () -> Unit,
    send: (String) -> Unit,
    resetScroll: () -> Unit,
    modifier: Modifier,
) {
    var textState by remember { mutableStateOf(TextFieldValue()) }
    var textFieldFocusState by remember { mutableStateOf(false) }
    val localSend = {
        send(textState.text)
        textState = TextFieldValue()
    }

    Row(modifier) {
        Control(onClick = selectPhoto) {
            Icon(
                painter = painterResource(id = R.drawable.ic_round_add_photo_alternate_24),
                contentDescription = ""
            )
        }
        UserInputText(
            textFieldValue = textState,
            onTextChanged = { textState = it },
            onTextFieldFocused = { focused ->
                if (focused) {
                    resetScroll()
                }
                textFieldFocusState = focused
            },
            focusState = textFieldFocusState,
            send = localSend
        )
        SendButton(
            enabled = textState.text.isNotBlank(),
            onClick = localSend
        )
    }
}

@Composable
private fun SendButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .height(36.dp),
        enabled = enabled,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            disabledBackgroundColor = Color.White.toColor(),
            disabledContentColor = Color.LightGray.toColor(),
        ),
        border = if (!enabled) {
            BorderStroke(
                width = 1.dp,
                color = Color.LightGray.toColor(),
            )
        } else null,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            "Send",
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun UserInputText(
    keyboardType: KeyboardType = KeyboardType.Text,
    onTextChanged: (TextFieldValue) -> Unit,
    textFieldValue: TextFieldValue,
    onTextFieldFocused: (Boolean) -> Unit,
    focusState: Boolean,
    send: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Surface {
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .weight(1f)
                    .align(Alignment.Bottom)
            ) {
                var lastFocusState by remember { mutableStateOf(false) }
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { onTextChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                        .align(Alignment.CenterStart)
                        .onFocusChanged { state ->
                            if (lastFocusState != state.isFocused) {
                                onTextFieldFocused(state.isFocused)
                            }
                            lastFocusState = state.isFocused
                        },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(onSend = { send() }),
                    maxLines = 1,
                    cursorBrush = SolidColor(LocalContentColor.current),
                    textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current)
                )

                val disableContentColor =
                    MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled)
                if (textFieldValue.text.isEmpty() && !focusState) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 16.dp),
                        text = "Message",
                        style = MaterialTheme.typography.body1.copy(color = disableContentColor)
                    )
                }
            }
        }
    }
}

private val JumpToBottomThreshold = 56.dp