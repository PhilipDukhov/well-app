package com.well.shared

expect class FirebaseManager() {
    companion object {
        val manager: FirebaseManager
    }

    suspend fun upload(byteArray: ByteArray, path: String): String
}
