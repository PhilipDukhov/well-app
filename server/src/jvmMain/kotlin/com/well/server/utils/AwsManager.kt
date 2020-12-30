package com.well.server.utils

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.event.ProgressEventType
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.S3ClientOptions
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.services.s3.transfer.Upload
import com.amazonaws.services.s3.transfer.model.UploadResult
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.io.ByteArrayInputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AwsManager(
    accessKeyId: String,
    secretAccessKey: String,
    private val bucketName: String
) {
    private val s3Client = AmazonS3Client(
        BasicAWSCredentials(accessKeyId, secretAccessKey)
    ).apply {
        setEndpoint("s3.us-east-2.amazonaws.com")
        setS3ClientOptions(
            S3ClientOptions.builder()
                .setPathStyleAccess(true)
                .build()
        )
    }

    private val transferManager = TransferManager(
        s3Client
    )

    suspend fun upload(
        data: ByteArray,
        path: String,
    ): Url =
        transferManager
            .upload(
                PutObjectRequest(
                    bucketName,
                    path,
                    ByteArrayInputStream(data),
                    ObjectMetadata().apply {
                        contentLength = data.count()
                            .toLong()
                    },
                ).withCannedAcl(CannedAccessControlList.PublicRead)
            )
            .await()
            .run {
                Url(s3Client.getResourceUrl(bucketName, key))
            }

    private suspend fun Upload.await(): UploadResult = suspendCoroutine { continuation ->
        addProgressListener {
            try {
                when (it.eventType) {
                    ProgressEventType.TRANSFER_FAILED_EVENT ->
                        continuation.resumeWithException(waitForException())
                    ProgressEventType.TRANSFER_COMPLETED_EVENT ->
                        continuation.resume(waitForUploadResult())
                    else -> Unit
                }
            } catch (t: Throwable) {
                continuation.resumeWithException(t)
            }
        }
    }
}

suspend fun Dependencies.uploadToS3FromUrl(
    url: Url,
    path: String,
) =
    localClient.get<HttpStatement>(url)
        .execute { response ->
            val fileExtension = response.contentType()
                ?.let { contentType ->
                    contentType.fileExtensions()
                        .first {
                            listOf(
                                "jpeg",
                                "jpg",
                                "png",
                            ).contains(it)
                        }
                } ?: return@execute null
            awsManager
                .upload(
                    response.readBytes(),
                    "$path.$fileExtension"
                )
        }