package com.well.modules.db.mobile

import com.well.modules.utils.AppContext
import com.squareup.sqldelight.db.SqlDriver

internal expect class DatabaseDriverFactory(appContext: AppContext) {
    fun createDriver(filename: String, schema: SqlDriver.Schema): SqlDriver
    fun deleteDatabase(filename: String)
}