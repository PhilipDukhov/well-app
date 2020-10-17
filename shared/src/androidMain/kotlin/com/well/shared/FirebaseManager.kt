package com.well.shared

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class FirebaseManager {
    actual companion object {
        actual val manager: FirebaseManager
            get() = FirebaseManager()
    }

    actual suspend fun upload(byteArray: ByteArray, path: String): String =
        suspendCancellableCoroutine { continuation ->
            val ref = Firebase.storage.reference.child(path)
            val uploadTask = ref.putBytes(byteArray)
            uploadTask
                .addOnProgressListener {
                    println("upload ${it.bytesTransferred.toDouble() / it.totalByteCount}")
                }
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(ref.downloadUrl.toString())
                    } else {
                        continuation.resumeWithException(task.exception ?: IllegalStateException())
                    }
                }
            continuation.cancel()
            continuation.invokeOnCancellation { uploadTask.cancel() }
        }
}
