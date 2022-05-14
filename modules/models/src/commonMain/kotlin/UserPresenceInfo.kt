package com.well.modules.models

import kotlinx.serialization.Serializable

@Serializable
class UserPresenceInfo(val id: User.Id, val lastEdited: Double)
