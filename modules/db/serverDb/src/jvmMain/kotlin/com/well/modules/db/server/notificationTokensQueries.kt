package com.well.modules.db.server

import com.well.modules.models.DeviceId
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.flow.map

fun NotificationTokensQueries.selectByDeviceIdFlow(deviceId: DeviceId) =
    selectByDeviceId(deviceId = deviceId)
        .asFlow()
        .mapToList()
        .map(List<NotificationTokens>::firstOrNull)