package com.well.shared

actual class FirebaseManager {
    actual companion object {
        actual val manager: FirebaseManager
            get() = FirebaseManager()
    }

    actual suspend fun upload(byteArray: ByteArray, path: String): String {
        return """TODO("Not yet implemented")"""
    }
}