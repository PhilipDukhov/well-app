package com.well.modules.models

import kotlinx.serialization.Serializable

@Serializable
data class UserPresenceInfo(val id: UserId, val lastEdited: Double)
