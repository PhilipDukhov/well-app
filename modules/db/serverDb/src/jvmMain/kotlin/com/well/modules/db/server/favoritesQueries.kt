package com.well.modules.db.server

import com.well.modules.models.User
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList

fun FavoritesQueries.listByOwnerIdFlow(ownerId: User.Id) =
    listByOwnerId(ownerId).asFlow().mapToList()
