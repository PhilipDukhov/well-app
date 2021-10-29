package com.well.modules.androidUi.composableScreens.userChat

import com.well.modules.androidUi.theme.body2Light
import com.well.modules.androidUi.customViews.LoadingCoilImage
import com.well.modules.androidUi.ext.backgroundKMM
import com.well.modules.androidUi.ext.thenOrNull
import com.well.modules.androidUi.ext.toColor
import com.well.modules.androidUi.ext.widthDp
import com.well.modules.models.Color
import com.well.modules.models.chat.ChatMessage
import com.well.modules.models.chat.ChatMessageWithStatus
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UserChatMessageCell(
    message: ChatMessageWithStatus,
) {
    val incoming = message.status.isIncoming
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        if (!incoming) {
            Spacer(
                modifier = Modifier
                    .weight(1f)
            )
        }
        Box(
            modifier = Modifier
                .weight(4f, fill = false)

        ) {
            Box(
                modifier = Modifier
                    .then(
                        if (incoming) {
                            Modifier.graphicsLayer(rotationY = 180f)
                        } else Modifier
                    )
                    .backgroundKMM(
                        color = if (incoming) Color.LightBlue else Color.LightGray,
                        shape = BubbleShape()
                    )
                    .matchParentSize()
            )

            Column(
                Modifier
                    .padding(
                        top = 5.dp,
                        bottom = 5.dp,
                        start = BubbleShape.radius
                            .plus(
                                if (incoming)
                                    BubbleShape.tailDx
                                else
                                    0.dp
                            ),
                        end = BubbleShape.radius
                            .plus(
                                if (!incoming)
                                    BubbleShape.tailDx
                                else
                                    0.dp
                            ),
                    )
            ) {
                ContentView(message.message.content)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        message.date + " ",
                        fontSize = 7.sp,
                        color = Color.White.toColor(),
                    )
                    message.status.icon()?.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White.toColor(),
                            modifier = Modifier.height(9.dp)
                        )
                    }
                }
            }
        }
        if (incoming) {
            Spacer(
                modifier = Modifier
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun ContentView(
    content: ChatMessage.Content,
) {
    when (content) {
        is ChatMessage.Content.Image -> {
            val width = LocalContext.current.resources.displayMetrics.widthDp / 2.5f
            Box(contentAlignment = Alignment.Center) {
                LoadingCoilImage(
                    data = content.url,
                    successProgressIndicatorNeeded = !content.url.startsWith("http"),
                    modifier = Modifier
                        .width(width)
                        .thenOrNull(
                            content.aspectRatio?.let { aspectRatio ->
                                Modifier.height(width / aspectRatio)
                            }
                        )
                )

            }
        }
        is ChatMessage.Content.Text -> {
            Text(
                content.string,
                style = MaterialTheme.typography.body2Light,
                color = Color.White.toColor(),
            )
        }
    }
}

private fun ChatMessageWithStatus.Status.icon() = when (this) {
    ChatMessageWithStatus.Status.IncomingUnread,
    ChatMessageWithStatus.Status.IncomingRead,
    -> null
    ChatMessageWithStatus.Status.OutgoingSending -> Icons.Default.Timer
    ChatMessageWithStatus.Status.OutgoingSent -> Icons.Default.Check
    ChatMessageWithStatus.Status.OutgoingRead -> Icons.Default.DoubleCheck
}