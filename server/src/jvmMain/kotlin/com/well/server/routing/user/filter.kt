package com.well.server.routing.user

import com.well.modules.models.UsersFilter
import com.well.modules.models.UsersFilter.Rating.*
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
    try {
        val filter = call.receive<UsersFilter>()
        val currentUid = call.authUid
        val users = database
            .usersQueries
            .filter(
                nameFilter = filter.searchString,
                favorites = filter.favorite.toLong(),
                uid = currentUid,
                skillsRegexp = filter.skills.adaptedAnyRegex(),
                academicRankRegexp = filter.academicRanks.adaptedAnyRegex(),
                languagesRegexp = filter.languages.adaptedAnyRegex(),
                countryCodeRegex = "",
                withReviews = filter.withReviews.toLong(),
                rating = filter.rating.toDoubleOrNull(),
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
    } catch (t: Throwable) {
        println("$")
        throw t
    }
}

fun Boolean.toLong(): Long = if (this) 1 else 0

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
    when(this) {
        All -> null
        Five -> 5.0
        Four -> 4.0
        Three -> 3.0
        Two -> 2.0
        One -> 1.0
    }