package com.well.modules.db.mobile.helper

import com.well.modules.utils.viewUtils.ApplicationContext
import co.touchlab.sqliter.DatabaseFileContext
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver

internal actual class DatabaseDriverFactory actual constructor(applicationContext: ApplicationContext) {
    actual fun createDriver(filename: String, schema: SqlDriver.Schema): SqlDriver {
        return NativeSqliteDriver(schema, filename)
    }
    actual fun deleteDatabase(filename: String) {
        DatabaseFileContext.deleteDatabase(filename)
    }
}