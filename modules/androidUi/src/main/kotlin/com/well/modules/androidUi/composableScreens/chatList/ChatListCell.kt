package com.well.modules.androidUi.composableScreens.chatList

import com.well.modules.androidUi.components.ProfileImage
import com.well.modules.androidUi.components.badgeLayout
import com.well.modules.androidUi.ext.backgroundKMM
import com.well.modules.androidUi.ext.toColor
import com.well.modules.androidUi.theme.captionLight
import com.well.modules.models.Color
import com.well.modules.features.chatList.chatListFeature.ChatListFeature as Feature
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private val padding = 16.dp

@Composable
fun ChatListCell(
    item: Feature.State.ListItem,
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
        .fillMaxWidth()
        .backgroundKMM(if (item.unreadCount > 0) Color.Green10 else Color.Transparent)
) {
    ProfileImage(
        item.user,
        modifier = Modifier
            .padding(padding)
            .size(40.dp)
    )
    Column(
        modifier = Modifier.padding(top = padding, end = padding)
    ) {
        Row {
            Text(
                text = item.user.fullName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.caption,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = item.lastMessage.date,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.captionLight,
                color = Color.DarkGrey.toColor(),
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = item.lastMessage.content.descriptionText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.captionLight,
                color = Color.DarkGrey.toColor(),
                modifier = Modifier
                    .padding(end = 10.dp)
                    .weight(1f, fill = false)
            )
            if (item.unreadCount > 0) {
                Text(
                    text = item.unreadCount.toString(),
                    style = MaterialTheme.typography.body2,
                    color = Color.White.toColor(),
                    modifier = Modifier
                        .background(Color.Green.toColor(), shape = RoundedCornerShape(100))
                        .badgeLayout()
                        .requiredWidth(IntrinsicSize.Min)
                )
            }
        }
        Spacer(modifier = Modifier.height(padding))
        Divider()
    }
}