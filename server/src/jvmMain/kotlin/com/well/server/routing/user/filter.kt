package com.well.server.routing.user

import com.well.modules.models.UsersFilter
import com.well.server.utils.Dependencies
import com.well.server.utils.authUid
import com.well.server.utils.toUser
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.filterUsers(
    dependencies: Dependencies
) = dependencies.run {
    val filter = call.receive<UsersFilter>()
    val currentUid = call.authUid
    val users = database
        .usersQueries
        .filter(
            nameFilter = filter.searchString,
            favorites = if (filter.favorite) 1 else 0,
            uid = currentUid,
            skillsRegexp = filter.skills.adaptedAnyRegex(),
            academicRankRegexp = filter.academicRanks.adaptedAnyRegex(),
            languagesRegexp = filter.languages.adaptedAnyRegex(),
        )
        .executeAsList()
        .map { user ->
            user.toUser(
                currentUid = currentUid,
                database = database,
            )
        }
    println("$filter\n$users")
    call.respond(users)
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