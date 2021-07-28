package com.well.modules.db.mobile

import com.well.modules.utils.AppContext
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver

internal actual class DatabaseDriverFactory actual constructor(private val appContext: AppContext) {
    actual fun createDriver(filename: String, schema: SqlDriver.Schema): SqlDriver =
        AndroidSqliteDriver(
            schema = schema,
            context = appContext.androidContext,
            name = filename,
        )

    actual fun deleteDatabase(filename: String) {
        TODO()
    }
}