package com.well.modules.db.server

import com.well.modules.models.User
import com.well.modules.models.chat.LastReadMessage
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList

fun LastReadMessages.toLastReadMessage() = LastReadMessage(
    fromId = fromId,
    messageId = messageId,
    peerId = peerId,
)

fun LastReadMessagesQueries.selectByPeerIdFlow(id: User.Id) =
    selectByPeerId(id)
        .asFlow()
        .mapToList()

