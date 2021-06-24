package com.well.modules.db.mobile

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import com.well.modules.utils.AppContext

internal actual class DatabaseDriverFactory actual constructor(appContext: AppContext) {
    actual fun createDriver(filename: String, schema: SqlDriver.Schema): SqlDriver {
        return NativeSqliteDriver(schema, filename)
    }
}