package com.well.serverModels

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: UserId,
    val firstName: String,
    val lastName: String,
    val type: Type,
    val profileImageUrl: String?,
) {
    @Serializable
    enum class Type {
        Facebook,
        Google,
        Test,
    }

    val fullName: String
        get() = "$firstName $lastName"
}