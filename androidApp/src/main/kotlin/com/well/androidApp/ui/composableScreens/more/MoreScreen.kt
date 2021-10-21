package com.well.androidApp.ui.composableScreens.more

import com.well.androidApp.ui.customViews.Control
import com.well.androidApp.ui.customViews.NavigationBar
import com.well.androidApp.ui.ext.toColor
import com.well.modules.models.Color
import com.well.sharedMobile.puerh.more.MoreFeature.Msg
import com.well.sharedMobile.puerh.more.MoreFeature.State
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Support
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ColumnScope.MoreScreen(
    state: State,
    listener: (Msg) -> Unit,
) {
    NavigationBar(title = "More")
    state.items.forEach { item ->
        Control(onClick = {
            listener(Msg.SelectItem(item))
        }) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = when (item) {
                        State.Item.Support -> Icons.Default.Support
                        State.Item.About -> Icons.Default.Info
                    },
                    contentDescription = "",
                    tint = Color.LightBlue.toColor()
                )
                Text(
                    item.name,
                    style = MaterialTheme.typography.body1,
                )
            }
        }
        Divider()
    }
}