package com.well.modules.androidUi.components.usersList

import com.well.modules.models.User
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun UsersList(
    users: List<User>,
    onSelect: (User) -> Unit,
    onToggleFavorite: (User) -> Unit,
    modifier: Modifier,
) {
    LazyColumn(modifier = modifier) {
        if (users.isNotEmpty()) {
            item { Divider() }
        }
        items(users) { user ->
            UserCell(
                user,
                onSelect = {
                    onSelect(user)
                },
                onToggleFavorite = {
                    onToggleFavorite(user)
                },
            )
            Divider()
        }
    }
}