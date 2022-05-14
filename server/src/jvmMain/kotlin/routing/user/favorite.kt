package com.well.server.routing.user

import com.well.modules.db.server.Favourites
import com.well.modules.models.FavoriteSetter
import com.well.server.utils.Services
import com.well.server.utils.authUid
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.setUserFavorite(
    services: Services,
) = services.run {
    val setFavorite = call.receive<FavoriteSetter>()
    val uid = call.authUid
    database.favoritesQueries.run databaseQuery@{
        val currentValue = isFavorite(uid, setFavorite.uid).executeAsOne()
        if (currentValue == setFavorite.favorite) return@databaseQuery
        if (setFavorite.favorite) {
            addFavorite(
                Favourites(
                    owner = uid,
                    destination = setFavorite.uid,
                )
            )
        } else {
            removeFavorite(uid, setFavorite.uid)
        }
    }
    call.respond(HttpStatusCode.OK)
}
