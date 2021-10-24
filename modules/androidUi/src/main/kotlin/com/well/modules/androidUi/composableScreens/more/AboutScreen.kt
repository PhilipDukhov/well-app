package com.well.modules.androidUi.composableScreens.more

import com.well.modules.androidUi.R
import com.well.modules.androidUi.theme.captionLight
import com.well.modules.androidUi.customViews.Control
import com.well.modules.androidUi.customViews.NavigationBar
import com.well.modules.androidUi.customViews.ProfileImage
import com.well.modules.androidUi.ext.toColor
import com.well.modules.models.Color
import com.well.modules.features.more.about.AboutFeature.Msg
import com.well.modules.features.more.about.AboutFeature.State
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun ColumnScope.AboutScreen(
    state: State,
    listener: (Msg) -> Unit,
) {
    NavigationBar(title = "About")
    state.teamMembers.forEach { teamMember ->
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            ProfileImage(
                image = teamMember.image,
                modifier = Modifier
                    .size(45.dp)
                    .padding(end = 10.dp)
            )
            Column {
                Text(
                    teamMember.name,
                    style = MaterialTheme.typography.caption,
                )
                Text(
                    teamMember.position,
                    style = MaterialTheme.typography.captionLight,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Control(onClick = {
                listener(Msg.OpenTwitter(teamMember))
            }) {
                Icon(
                    painter = painterResource(R.drawable.ic_twitter),
                    contentDescription = null,
                    tint = Color.LightBlue.toColor(),
                )
            }
        }
    }
    Text(
        state.text,
        style = MaterialTheme.typography.body1,
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 10.dp)
    )
}