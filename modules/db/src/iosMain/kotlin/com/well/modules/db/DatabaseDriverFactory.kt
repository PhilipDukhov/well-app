package com.well.modules.db

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import com.well.modules.utils.AppContext

internal actual class DatabaseDriverFactory actual constructor(appContext: AppContext) {
    actual fun createDriver(filename: String): SqlDriver {
        return NativeSqliteDriver(Database.Schema, filename)
    }
}