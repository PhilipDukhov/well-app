package com.well.modules.db.users

import com.well.modules.models.User
import com.well.modules.models.UserPresenceInfo
import com.well.modules.utils.flowUtils.mapIterable
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

fun UsersQueries.usersPresenceFlow() =
    getAllIdsWithEdited()
        .asFlow()
        .mapToList()
        .map { list ->
            list.map { UserPresenceInfo(id = it.id, lastEdited = it.lastEdited) }
        }

fun UsersQueries.getByIdFlow(uid: User.Id) =
    getById(uid)
        .asFlow()
        .mapToOneOrNull()
        .filterNotNull()
        .map(Users::toUser)

fun UsersQueries.getByIdsFlow(uids: Collection<User.Id>) =
    getByIds(uids)
        .asFlow()
        .mapToList()
        .mapIterable(Users::toUser)

fun UsersQueries.getFavoritesFlow() =
    getFavorites()
        .asFlow()
        .mapToList()
        .filterNotNull()
        .mapIterable(Users::toUser)