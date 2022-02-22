package com.well.modules.utils.viewUtils

import okio.Path
import okio.Path.Companion.toPath

expect class ApplicationContext {
    val documentsDir: String
    suspend fun collectLogs(): Path
}

val ApplicationContext.logsDir get() = documentsDir.toPath() / "logs"