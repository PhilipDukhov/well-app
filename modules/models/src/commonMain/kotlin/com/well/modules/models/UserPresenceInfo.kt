package com.well.modules.models

import kotlinx.serialization.Serializable

@Serializable
data class UserPresenceInfo(val id: User.Id, val lastEdited: Double)
