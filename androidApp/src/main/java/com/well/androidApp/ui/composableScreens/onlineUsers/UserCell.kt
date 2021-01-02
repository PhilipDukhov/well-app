package com.well.androidApp.ui.composableScreens.onlineUsers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.well.androidApp.ui.composableScreens.zCustomViews.UserProfileImage
import com.well.serverModels.User

private val padding = 16.dp

@Composable
fun UserCell(
    user: User,
    onClick: () -> Unit
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(padding)
) {
    UserProfileImage(
        user,
        modifier = Modifier
            .size(30.dp)
    )
    Spacer(modifier = Modifier.width(padding))
    Text(
        text = user.fullName,
        maxLines = 1,
    )
}