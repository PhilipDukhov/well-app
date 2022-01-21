package com.well.server.utils

import com.well.modules.models.DeviceId
import com.well.modules.models.User

data class ClientKey(val deviceId: DeviceId, val uid: User.Id)
