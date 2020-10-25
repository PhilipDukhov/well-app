package com.well.shared

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

actual class FirebaseManager {
    actual companion object {
        actual val manager: FirebaseManager
            get() = FirebaseManager()
    }

    actual suspend fun upload(byteArray: ByteArray, path: String): String {
        val ref = Firebase.storage.reference.child(path)
        val uploadTask = ref.putBytes(byteArray)
        return uploadTask
            .addOnProgressListener {
                println("upload ${it.bytesTransferred.toDouble() / it.totalByteCount}")
            }
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                ref.downloadUrl
            }
            .await().toString()
    }
}
