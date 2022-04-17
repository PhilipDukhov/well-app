package com.well.modules.androidUi.composableScreens.more

import com.well.modules.androidUi.R
import com.well.modules.androidUi.components.Control
import com.well.modules.androidUi.components.NavigationBar
import com.well.modules.androidUi.ext.toColor
import com.well.modules.features.more.moreFeature.MoreFeature.Msg
import com.well.modules.features.more.moreFeature.MoreFeature.State
import com.well.modules.models.Color
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
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Support
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun ColumnScope.MoreScreen(
    state: State,
    listener: (Msg) -> Unit,
) {
    NavigationBar(title = state.title)
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
                    painter = when (item) {
                        State.Item.TechnicalSupport -> rememberVectorPainter(Icons.Default.Support)
                        State.Item.About -> rememberVectorPainter(Icons.Default.Info)
                        State.Item.WellAcademy -> painterResource(R.drawable.ic_well_academy)
                        State.Item.InviteColleague -> rememberVectorPainter(Icons.Outlined.PersonAddAlt1)
                        State.Item.Favorites -> rememberVectorPainter(Icons.Default.FavoriteBorder)
                        State.Item.ActivityHistory -> rememberVectorPainter(Icons.Default.History)
                        State.Item.Donate -> rememberVectorPainter(Icons.Default.SentimentSatisfiedAlt)
                    },
                    contentDescription = null,
                    tint = Color.LightBlue.toColor()
                )
                Text(
                    item.title,
                    style = MaterialTheme.typography.body1,
                )
            }
        }
        Divider()
    }
}