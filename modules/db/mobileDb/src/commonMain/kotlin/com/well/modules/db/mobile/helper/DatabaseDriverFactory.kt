package com.well.modules.db.mobile.helper

import com.well.modules.utils.viewUtils.AppContext
import com.squareup.sqldelight.db.SqlDriver

internal expect class DatabaseDriverFactory(appContext: AppContext) {
    fun createDriver(filename: String, schema: SqlDriver.Schema): SqlDriver
    fun deleteDatabase(filename: String)
}