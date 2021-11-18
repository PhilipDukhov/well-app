package com.well.modules.db.server

import com.well.modules.models.Availability
import com.well.modules.models.User
import com.well.modules.models.chat.ChatMessage
import com.well.modules.models.chat.LastReadMessage
import kotlinx.datetime.Instant
import java.util.*

fun ChatMessagesQueries.insertChatMessage(message: ChatMessage): ChatMessage =
    transactionWithResult {
        insert(
            creation = Date().time.toDouble() / 1000,
            fromId = message.fromId,
            peerId = message.peerId,
            text = message.content.text,
            photoUrl = message.content.photoUrl,
            photoAspectRatio = message.content.photoAspectRatio,
        )
        val id = ChatMessage.Id(lastInsertId().executeAsOne())
        getById(id)
            .executeAsOne()
            .toChatMessage()
    }

fun UsersQueries.updateUser(user: User) = user.apply {
    updateUser(
        id = id,
        fullName = fullName,
        email = email,
        profileImageUrl = profileImageUrl,
        phoneNumber = phoneNumber,
        countryCode = countryCode,
        timeZoneIdentifier = timeZoneIdentifier,
        credentials = credentials,
        academicRank = academicRank,
        languages = languages,
        skills = skills,
        bio = bio,
        education = education,
        professionalMemberships = professionalMemberships,
        publications = publications,
        twitter = twitter,
        doximity = doximity,
    )
}

fun ChatMessages.toChatMessage() = ChatMessage(
    id = id,
    creation = creation,
    fromId = fromId,
    peerId = peerId,
    content = if (photoUrl != null) ChatMessage.Content.Image(photoUrl,
        aspectRatio = photoAspectRatio) else ChatMessage.Content.Text(text),
)

fun LastReadMessages.toLastReadMessage() = LastReadMessage(
    fromId = fromId,
    messageId = messageId,
    peerId = peerId,
)

fun Availabilities.toAvailability() = Availability(
    id = id,
    startInstant = Instant.fromEpochMilliseconds(startInstantEpochMilliseconds),
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
            startInstantEpochMilliseconds = availability.startInstant.toEpochMilliseconds(),
            durationMinutes = availability.durationMinutes,
            repeat = availability.repeat,
        )
        getById(
            Availability.Id(
                lastInsertId()
                    .executeAsOne()
            )
        ).executeAsOne().toAvailability()
    }