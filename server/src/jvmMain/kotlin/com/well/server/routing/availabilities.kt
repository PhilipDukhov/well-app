package com.well.server.routing

import com.well.modules.db.server.getByOwnerId
import com.well.modules.db.server.insert
import com.well.modules.models.Availability
import com.well.modules.models.User
import com.well.server.utils.Dependencies
import com.well.server.utils.authUid
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.listAvailabilities(
    id: User.Id,
    dependencies: Dependencies,
) = dependencies.run {
    val availabilities = database.availabilitiesQueries.getByOwnerId(id)
    call.respond(HttpStatusCode.OK, availabilities)
}

suspend fun PipelineContext<*, ApplicationCall>.userHasAvailableAvailabilities(
    id: User.Id,
    dependencies: Dependencies,
) = dependencies.run {
    println("getByOwnerId ${database.availabilitiesQueries.getByOwnerId(id)}")
    println("userHasAvailabilities ${database.availabilitiesQueries.userHasAvailabilities(id).executeAsOne()}")
    call.respond(
        HttpStatusCode.OK,
        database.availabilitiesQueries.userHasAvailabilities(id).executeAsOne()
    )
}

suspend fun PipelineContext<*, ApplicationCall>.putAvailability(
    dependencies: Dependencies,
) = dependencies.run {
    val availability = call.receive<Availability>()
    val userId = call.authUid
    val newAvailability: Availability = database.availabilitiesQueries.run {
        transactionWithResult {
            if (availability.id.value >= 0) {
                markDeleted(availability.id)
            }
            insert(
                ownerId = userId,
                availability = availability,
            )
        }
    }
    call.respond(HttpStatusCode.OK, newAvailability)
}

suspend fun PipelineContext<*, ApplicationCall>.deleteAvailability(
    id: Availability.Id,
    dependencies: Dependencies,
) = dependencies.run {
    database.availabilitiesQueries.markDeleted(id)
    call.respond(HttpStatusCode.OK)
}
