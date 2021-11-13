package com.well.modules.utils.viewUtils.dataStore

import com.well.modules.models.User
import kotlinx.serialization.Serializable

@Serializable
data class AuthInfo(val token: String, val id: User.Id)