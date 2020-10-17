package com.well.shared

actual class FirebaseManager {
    actual companion object {
        actual val manager: FirebaseManager
            get() = TODO("Not yet implemented")
    }

    actual suspend fun upload(byteArray: ByteArray, path: String): String {
        TODO("Not yet implemented")
    }
}