package com.well.modules.db.chatMessages

import com.well.modules.models.Meeting
import com.well.modules.models.User
import com.well.modules.models.chat.ChatMessage
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.db.SqlDriver

fun ChatMessagesDatabase.Companion.create(driver: SqlDriver) = ChatMessagesDatabase(
    driver,
    ChatMessagesAdapter = ChatMessages.Adapter(
        fromIdAdapter = User.Id.ColumnAdapter,
        peerIdAdapter = User.Id.ColumnAdapter,
        idAdapter = ChatMessage.Id.ColumnAdapter,
        contentTypeAdapter = EnumColumnAdapter(),
    ),
    LastReadMessagesAdapter = LastReadMessages.Adapter(
        fromIdAdapter = User.Id.ColumnAdapter,
        peerIdAdapter = User.Id.ColumnAdapter,
        messageIdAdapter = ChatMessage.Id.ColumnAdapter,
    ),
    TmpMessageIdsAdapter = TmpMessageIds.Adapter(
        idAdapter = ChatMessage.Id.ColumnAdapter
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
)