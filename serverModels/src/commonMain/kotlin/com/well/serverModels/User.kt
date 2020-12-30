package com.well.serverModels

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: UserId,
    val firstName: String,
    val lastName: String,
    val type: Type,
) {
    @kotlinx.serialization.Transient
    val imageUrl: String = "https://i.picsum.photos/id/1016/300/300.jpg?hmac=bjfLAoQLUHytpHr4nBTho_zxJd0gRuvCAv9U5rXpnNA"

    @Serializable
    enum class Type {
        Facebook,
        Google,
    }

    val fullName: String
        get() = "$firstName $lastName"
}