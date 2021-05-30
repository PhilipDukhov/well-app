package com.well.server.routing.user

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.well.modules.models.UserId
import com.well.modules.models.UserPresenceInfo
import com.well.modules.models.UsersFilter
import com.well.modules.models.WebSocketMsg
import com.well.server.utils.Dependencies
import com.well.server.utils.send
import com.well.server.utils.toLong
import com.well.server.utils.toUser
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class UserSession(
    private val currentUid: UserId,
    private val webSocketSession: WebSocketSession,
    private val dependencies: Dependencies,
) : WebSocketSession by webSocketSession {
    val expertsFilterFlow = MutableStateFlow<UsersFilter?>(null)
    val usersPresenceInfoFlow = MutableStateFlow<List<UserPresenceInfo>?>(null)
    private val expertsFlow: Flow<List<UserId>> = expertsFilterFlow
        .filterNotNull()
        .flatMapLatest { filter ->
            dependencies.database.usersQueries
                .filter(
                    nameFilter = filter.searchString,
                    favorites = filter.favorite.toLong(),
                    uid = currentUid,
                    specificIdsRegexp = "",
                    skillsRegexp = filter.skills.adaptedAnyRegex(),
                    academicRankRegexp = filter.academicRanks.adaptedAnyRegex(),
                    languagesRegexp = filter.languages.adaptedAnyRegex(),
                    countryCode = filter.countryCode ?: "",
                    withReviews = filter.withReviews.toLong(),
                    rating = filter.rating.toDoubleOrNull(),
                )
                .asFlow()
                .mapToList()
        }
    private val neededUsersFlow = expertsFlow
        .map {
            it.toMutableSet().apply {
                add(currentUid)
            }.toSet()
        }
    private val notUpToDateUsers = usersPresenceInfoFlow
        .filterNotNull()
        .combine(neededUsersFlow) { usersPresenceInfo, neededUsers ->
            dependencies.database
                .usersQueries
                .getByIds(neededUsers)
                .asFlow()
                .mapToList()
                .map { users ->
                    users.filter { user ->
                        usersPresenceInfo
                            .firstOrNull { it.id == user.id }
                            ?.lastEdited?.let { presenceLastEdited ->
                                user.lastEdited > presenceLastEdited
                            } ?: true
                    }.map { it.toUser(currentUid, dependencies.database) }
                }
        }
        .flatMapLatest()
        .filter { it.isNotEmpty() }

    init {
        CoroutineScope(coroutineContext).launch {
            expertsFlow
                .map(WebSocketMsg::ListFilteredExperts)
                .collect(::send)
        }
        CoroutineScope(coroutineContext).launch {
            notUpToDateUsers
                .map(WebSocketMsg::UpdateUsers)
                .collect(::send)
        }
    }
}

private inline fun <reified T : Enum<T>> Set<Enum<T>>.adaptedAnyRegex() =
    if (isEmpty())
        ""
    else
        joinToString(
            separator = "|",
            prefix = "(,|^)(",
            postfix = "(,|\$))"
        ) { it.name }

private fun UsersFilter.Rating.toDoubleOrNull(): Double? =
    when (this) {
        UsersFilter.Rating.All -> null
        UsersFilter.Rating.Five -> 5.0
        UsersFilter.Rating.Four -> 4.0
        UsersFilter.Rating.Three -> 3.0
        UsersFilter.Rating.Two -> 2.0
        UsersFilter.Rating.One -> 1.0
    }

fun <T> Flow<Flow<T>>.flatMapLatest(): Flow<T> = flatMapLatest { it }
