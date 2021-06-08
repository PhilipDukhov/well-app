package com.well.modules.db.users

import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.well.modules.utils.AppContext

internal actual class DatabaseDriverFactory actual constructor(private val appContext: AppContext) {
    actual fun createDriver(filename: String): SqlDriver =
        AndroidSqliteDriver(
            schema = Database.Schema,
            context = appContext.androidContext,
            name = filename,
        )
}