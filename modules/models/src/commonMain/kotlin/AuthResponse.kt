package com.well.modules.models

import kotlinx.serialization.Serializable

@Serializable
class AuthResponse(val token: String, val user: User)