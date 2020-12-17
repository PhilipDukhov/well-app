package com.well.serverModels

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: UserId,
    val firstName: String,
    val lastName: String,
    val type: Type,
) {
    @Serializable
    enum class Type {
        Facebook,
        Google,
    }

    val fullName: String
        get() = "$firstName $lastName"
}