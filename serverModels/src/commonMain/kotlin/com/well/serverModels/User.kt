package com.well.serverModels

import kotlinx.serialization.Serializable


@Serializable
data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val type: Type,
) {
    @Serializable
    enum class Type {
        Facebook,
        Google,
    }
}