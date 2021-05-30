package com.well.modules.utils.dataStore

import kotlinx.serialization.Serializable

@Serializable
data class AuthInfo(val token: String, val id: Int)
