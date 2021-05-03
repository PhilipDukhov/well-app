package com.well.server.routing.user

import com.well.modules.models.UsersFilter
import com.well.server.utils.Dependencies
import com.well.server.utils.authUserId
import com.well.server.utils.toUser
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.filterUsers(
    dependencies: Dependencies
) = dependencies.run {
    val filter = call.receive<UsersFilter>()
    val currentUserId = call.authUserId
    val users = database
        .usersQueries
        .filter(
            nameFilter = filter.searchString,
            favorites = if (filter.favorite) 1 else 0,
            userId = call.authUserId,
        )
        .executeAsList()
        .map { user ->
            user.toUser(
                database.favoritesQueries
                    .isFavorite(currentUserId, user.id)
                    .executeAsOne()
            )
        }
    call.respond(users)
}