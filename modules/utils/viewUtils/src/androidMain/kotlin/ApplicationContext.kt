package com.well.modules.utils.viewUtils

import android.content.Context
import androidx.annotation.DrawableRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path
import okio.Path.Companion.toOkioPath
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

actual data class ApplicationContext(
    val context: Context,
    @DrawableRes val notificationResId: Int,
    val activityClass: Class<*>,
) {
    actual val documentsDir: String
        get() = context.filesDir.path

    actual suspend fun collectLogs(): Path {
        val inputDirectory = logsDir.toFile()
        return withContext(Dispatchers.IO) {
            val outputZipFile = File.createTempFile("logs", ".zip")
            ZipOutputStream(BufferedOutputStream(FileOutputStream(outputZipFile))).use { zipOut ->
                inputDirectory.walkTopDown().forEach { file ->
                    val zipFileName = file.absolutePath.removePrefix(inputDirectory.absolutePath)
                        .removePrefix("/")
                    val entry = ZipEntry("$zipFileName${(if (file.isDirectory) "/" else "")}")
                    zipOut.putNextEntry(entry)
                    if (file.isFile) {
                        file.inputStream().copyTo(zipOut)
                    }
                }
            }
            outputZipFile.toOkioPath()
        }
    }
}