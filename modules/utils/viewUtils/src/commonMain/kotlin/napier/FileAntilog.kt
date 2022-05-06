package com.well.modules.utils.viewUtils.napier

import com.well.modules.models.date.dateTime.durationSince
import com.well.modules.utils.viewUtils.ApplicationContext
import com.well.modules.utils.viewUtils.logsDir
import com.well.modules.utils.viewUtils.platform.Platform
import com.well.modules.utils.viewUtils.platform.fileSystem
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import okio.Path
import okio.buffer
import kotlin.time.Duration.Companion.days

class FileAntilog(applicationContext: ApplicationContext) : Antilog() {
    private val logFile: Path
    private val fileSystem = Platform.fileSystem

    init {
        val logsDir = applicationContext.logsDir
        fileSystem.createDirectory(logsDir, mustCreate = false)
        val files = fileSystem.list(logsDir)
        val extension = ".txt"
        val now = Clock.System.now()
        files.forEach {
            try {
                val instant = Instant.parse(it.name.dropLast(extension.count()))
                if (-instant.durationSince(now) > 1.days) {
                    Napier.d("delete old $it")
                    fileSystem.delete(it)
                }
            } catch (e: Exception) {
                Napier.d("delete un-parsed $it")
                fileSystem.delete(it)
            }
        }
        logFile = logsDir / "$now$extension"
    }

    override fun performLog(priority: LogLevel, tag: String?, throwable: Throwable?, message: String?) {
        val logTag = tag ?: ""

        val fullMessage = if (message != null) {
            if (throwable != null) {
                "$message\n${throwable.message}"
            } else {
                message
            }
        } else throwable?.message ?: return

        fileSystem.openReadWrite(logFile)
            .appendingSink().buffer()
            .writeUtf8("${Clock.System.now()} ${priority.nameSpaced}| $logTag: $fullMessage\n")
            .close()
    }

    private val maxPriorityLength = LogLevel.values().maxOf { it.name.length }

    private val LogLevel.nameSpaced get() = name + " ".repeat(maxPriorityLength - name.length + 1)
}