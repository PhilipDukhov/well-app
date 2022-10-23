package com.well.server.routing

import com.well.modules.db.server.AvailabilitiesQueries
import com.well.modules.db.server.getByOwnerId
import com.well.modules.db.server.insert
import com.well.modules.models.Availability
import com.well.modules.models.BookingAvailabilitiesListByDay
import com.well.modules.models.BookingAvailability
import com.well.modules.models.User
import com.well.modules.utils.kotlinUtils.mapSecond
import com.well.server.utils.Services
import com.well.server.utils.ForbiddenException
import com.well.server.utils.authUid
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import kotlin.time.Duration.Companion.hours

suspend fun PipelineContext<*, ApplicationCall>.listCurrentAvailabilities(
    services: Services,
) = with(services) {
    val availabilities = database.availabilitiesQueries.getByOwnerId(call.authUid)
    call.respond(HttpStatusCode.OK, availabilities)
}

suspend fun PipelineContext<*, ApplicationCall>.listBookingAvailabilities(
    id: User.Id,
    services: Services,
) = call.respond(
    HttpStatusCode.OK,
    getBookingAvailabilities(id, services)
)

suspend fun PipelineContext<*, ApplicationCall>.userHasAvailableAvailabilities(
    id: User.Id,
    services: Services,
) = call.respond(
    HttpStatusCode.OK,
    getBookingAvailabilities(id, services).isNotEmpty()
)

private fun getBookingAvailabilities(
    id: User.Id,
    services: Services,
): BookingAvailabilitiesListByDay = with(services) {
    val allAvailabilities = database.availabilitiesQueries.getByOwnerId(id)

    val possibleAvailabilities = BookingAvailability
        .createWithAvailabilities(
            availabilities = allAvailabilities,
            days = 30,
            minInterval = 1.hours
        )
    with(database.meetingsQueries) {
        transactionWithResult {
            possibleAvailabilities
                .map {
                    it.mapSecond {
                        it.filterNot { bookingAvailability ->
                            database.meetingsQueries.isTaken(
                                availabilityId = bookingAvailability.availabilityId,
                                startInstant = bookingAvailability.startInstant,
                            ).executeAsOne()
                        }
                    }
                }
                .filter { it.second.isNotEmpty() }
        }
    }
}

suspend fun PipelineContext<*, ApplicationCall>.putAvailability(
    services: Services,
) = with(services) {
    val availability = call.receive<Availability>()
    val userId = call.authUid
    val newAvailability: Availability = with(database.availabilitiesQueries) {
        transactionWithResult {
            if (availability.id.value >= 0) {
                verityOwner(availabilityId = availability.id, ownerId = userId)
                markDeleted(availability.id)
            }
            intersects(
                ownerId = userId,
                startInstant = availability.startInstant.toEpochMilliseconds(),
                endInstant = availability.endInstant
            )
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
    services: Services,
) = with(services) {
    with(database.availabilitiesQueries) {
        verityOwner(availabilityId = id, ownerId = call.authUid)
        database.availabilitiesQueries.markDeleted(id)
    }
    call.respond(HttpStatusCode.OK)
}

suspend fun PipelineContext<*, ApplicationCall>.bookAvailability(
    services: Services,
) = with(services) {
    val bookingRequest = call.receive<BookingAvailability>()
    val success = database.transactionWithResult<Boolean> {
        val availability = database.availabilitiesQueries
            .getById(bookingRequest.availabilityId)
            .executeAsOneOrNull()
        if (availability == null || availability.deleted) {
            return@transactionWithResult false
        }
        if (
            database.meetingsQueries
                .isTaken(availability.id, bookingRequest.startInstant)
                .executeAsOne()
        ) {
            return@transactionWithResult false
        }
        val currentUid = call.authUid
        val newMeetingId = database.meetingsQueries
            .insert(
                availability = availability,
                bookingAvailability = bookingRequest,
                attendeeId = currentUid,
            )
        services.deliverMeetingNotification(newMeetingId, currentUid)
        return@transactionWithResult true
    }
    call.respond(if (success) HttpStatusCode.Created else HttpStatusCode.NotFound)
}

private fun AvailabilitiesQueries.verityOwner(availabilityId: Availability.Id, ownerId: User.Id) {
    if (!isOwnerEquals(ownerId = ownerId, id = availabilityId).executeAsOne()) {
        throw ForbiddenException()
    }
}