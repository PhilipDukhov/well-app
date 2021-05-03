package com.well.server.routing.user

import com.well.modules.models.FavoriteSetter
import com.well.server.utils.Dependencies
import com.well.server.utils.authUserId
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.setUserFavorite(
    dependencies: Dependencies
) = dependencies.run {
    val setFavorite = call.receive<FavoriteSetter>()
    val userId = call.authUserId
    database.favoritesQueries.run databaseQuery@{
        val currentValue = isFavorite(userId, setFavorite.userId).executeAsOne()
        if (currentValue == setFavorite.favorite) return@databaseQuery
        if (setFavorite.favorite) {
            addFavorite(userId, setFavorite.userId)
        } else {
            removeFavorite(userId, setFavorite.userId)
        }
    }
    call.respond(HttpStatusCode.OK)
}
