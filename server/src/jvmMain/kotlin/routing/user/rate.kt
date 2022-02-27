package com.well.server.routing.user

import com.well.modules.db.server.Ratings
import com.well.modules.models.RatingRequest
import com.well.server.utils.Dependencies
import com.well.server.utils.authUid
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.rate(
    dependencies: Dependencies,
) = dependencies.run {
    val request = call.receive<RatingRequest>()
    val currentUid = call.authUid
    database.transaction {
        database
            .ratingQueries
            .delete(
                owner = currentUid,
                destination = request.uid,
            )
        database
            .ratingQueries
            .add(
                Ratings(
                    owner = currentUid,
                    destination = request.uid,
                    value_ = request.review.value,
                    text = request.review.text
                )
            )
        val userRatingInfo = database
            .ratingQueries
            .userRating(request.uid)
            .executeAsOne()
        database
            .usersQueries
            .updateRating(
                id = request.uid,
                average = userRatingInfo.AVG ?: 0.0,
                count = userRatingInfo.COUNT.toInt(),
            )
    }
    call.respond(HttpStatusCode.OK)
}