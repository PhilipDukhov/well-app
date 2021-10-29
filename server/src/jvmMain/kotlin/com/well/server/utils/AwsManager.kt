package com.well.server.utils

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.event.ProgressEventType
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.transfer.TransferManagerBuilder
import com.amazonaws.services.s3.transfer.Upload
import com.amazonaws.services.s3.transfer.model.UploadResult
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import java.io.ByteArrayInputStream

class AwsManager(
    accessKeyId: String,
    secretAccessKey: String,
    private val bucketName: String
) {
    private val s3Client = (AmazonS3Client.builder()
        .withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials(accessKeyId, secretAccessKey)))
        .apply {
            setEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("s3.us-east-2.amazonaws.com", "us-east-2"))
            isPathStyleAccessEnabled = true
        }
        .build() as AmazonS3Client)

    private val transferManager = TransferManagerBuilder.standard()
        .withS3Client(s3Client)
        .build()

    suspend fun upload(
        data: ByteArray,
        path: String,
    ): Url = transferManager
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

    fun exists(path: String) = s3Client.doesObjectExist(bucketName, path)

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
) = client.get<HttpStatement>(url)
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