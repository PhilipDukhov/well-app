package com.well.androidApp.ui.composableScreens.chatList

import com.well.androidApp.ui.theme.captionLight
import com.well.androidApp.ui.customViews.ProfileImage
import com.well.androidApp.ui.ext.backgroundKMM
import com.well.androidApp.ui.ext.toColor
import com.well.modules.models.Color
import com.well.sharedMobile.puerh.chatList.ChatListFeature
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
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

private val padding = 16.dp

@Composable
fun ChatListCell(
    item: ChatListFeature.State.ListItem
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
                text = item.lastMessage.message.contentDescription(),
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
                        .widthGreaterOrEqualThanHeight()
                        .requiredWidth(IntrinsicSize.Min)
                        .padding(horizontal = 5.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(padding))
        Divider()
    }
}

private fun Modifier.widthGreaterOrEqualThanHeight() = this.then(object : LayoutModifier {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val textPlaceable = measurable.measure(constraints)
        val width = maxOf(textPlaceable.width, textPlaceable.height)
        return layout(width, textPlaceable.height) {
            textPlaceable.place((width - textPlaceable.width) / 2, 0)
        }
    }
})
