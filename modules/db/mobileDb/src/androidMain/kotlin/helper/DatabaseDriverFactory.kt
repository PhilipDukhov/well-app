package com.well.modules.db.mobile.helper

import com.well.modules.utils.viewUtils.ApplicationContext
import com.well.modules.utils.viewUtils.SystemContext
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver

internal actual class DatabaseDriverFactory actual constructor(private val applicationContext: ApplicationContext) {
    actual fun createDriver(filename: String, schema: SqlDriver.Schema): SqlDriver =
        AndroidSqliteDriver(
            schema = schema,
            context = applicationContext.context,
            name = filename,
        )

    actual fun deleteDatabase(
        filename: String,
        systemContext: SystemContext,
    ) {
        systemContext.activity.deleteDatabase(filename)
    }
}