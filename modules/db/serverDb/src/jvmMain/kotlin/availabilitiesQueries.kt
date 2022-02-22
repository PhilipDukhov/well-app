package com.well.modules.db.server

import com.well.modules.models.Availability
import com.well.modules.models.User

fun Availabilities.toAvailability() = Availability(
    id = id,
    startInstant = startInstant,
    durationMinutes = durationMinutes,
    repeat = repeat,
)

fun AvailabilitiesQueries.getByOwnerId(id: User.Id) =
    getByOwnerIdQuery(id)
        .executeAsList()
        .map(Availabilities::toAvailability)

fun AvailabilitiesQueries.insert(ownerId: User.Id, availability: Availability): Availability =
    transactionWithResult {
        insert(
            ownerId = ownerId,
            startInstant = availability.startInstant,
            durationMinutes = availability.durationMinutes,
            repeat = availability.repeat,
        )
        getById(
            Availability.Id(lastInsertId().executeAsOne())
        ).executeAsOne().toAvailability()
    }