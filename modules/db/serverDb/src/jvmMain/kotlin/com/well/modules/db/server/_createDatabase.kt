package com.well.modules.db.server

import com.well.modules.models.Availability
import com.well.modules.models.DeviceId
import com.well.modules.models.Meeting
import com.well.modules.models.User
import com.well.modules.models.chat.ChatMessage
import com.well.modules.utils.dbUtils.InstantColumnAdapter
import com.well.modules.utils.dbUtils.SerializableColumnAdapter
import com.well.modules.utils.dbUtils.SetEnumColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.db.SqlDriver

operator fun Database.Companion.invoke(driver: SqlDriver) =
    Database(
        driver = driver,
        UsersAdapter = Users.Adapter(
            typeAdapter = EnumColumnAdapter(),
            credentialsAdapter = EnumColumnAdapter(),
            academicRankAdapter = EnumColumnAdapter(),
            languagesAdapter = SetEnumColumnAdapter(),
            skillsAdapter = SetEnumColumnAdapter(),
            idAdapter = User.Id.ColumnAdapter,
        ),
        AvailabilitiesAdapter = Availabilities.Adapter(
            repeatAdapter = EnumColumnAdapter(),
            ownerIdAdapter = User.Id.ColumnAdapter,
            idAdapter = Availability.Id.ColumnAdapter,
            startInstantAdapter = InstantColumnAdapter,
        ),
        ChatMessagesAdapter = ChatMessages.Adapter(
            fromIdAdapter = User.Id.ColumnAdapter,
            peerIdAdapter = User.Id.ColumnAdapter,
            idAdapter = ChatMessage.Id.ColumnAdapter,
            contentTypeAdapter = EnumColumnAdapter(),
        ),
        FavouritesAdapter = Favourites.Adapter(
            ownerAdapter = User.Id.ColumnAdapter,
            destinationAdapter = User.Id.ColumnAdapter,
        ),
        NotificationTokensAdapter = NotificationTokens.Adapter(
            tokenAdapter = SerializableColumnAdapter(),
            uidAdapter = User.Id.ColumnAdapter,
            deviceIdAdapter = DeviceId.ColumnAdapter,
            timestampAdapter = InstantColumnAdapter,
        ),
        LastReadMessagesAdapter = LastReadMessages.Adapter(
            fromIdAdapter = User.Id.ColumnAdapter,
            peerIdAdapter = User.Id.ColumnAdapter,
            messageIdAdapter = ChatMessage.Id.ColumnAdapter,
        ),
        RatingsAdapter = Ratings.Adapter(
            ownerAdapter = User.Id.ColumnAdapter,
            destinationAdapter = User.Id.ColumnAdapter,
        ),
        ChatContentImagesAdapter = ChatContentImages.Adapter(
            messageIdAdapter = ChatMessage.Id.ColumnAdapter,
        ),
        ChatContentMeetingsAdapter = ChatContentMeetings.Adapter(
            messageIdAdapter = ChatMessage.Id.ColumnAdapter,
            meetingIdAdapter = Meeting.Id.ColumnAdapter,
        ),
        ChatContentTextsAdapter = ChatContentTexts.Adapter(
            messageIdAdapter = ChatMessage.Id.ColumnAdapter,
        ),
        MeetingsAdapter = Meetings.Adapter(
            idAdapter = Meeting.Id.ColumnAdapter,
            availabilityIdAdapter = Availability.Id.ColumnAdapter,
            startInstantAdapter = InstantColumnAdapter,
            attendeesAdapter = User.Id.SetColumnAdapter,
        ),
    )