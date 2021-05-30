package com.well.modules.db

import com.squareup.sqldelight.db.SqlDriver
import com.well.modules.utils.AppContext

internal expect class DatabaseDriverFactory(appContext: AppContext) {
    fun createDriver(filename: String): SqlDriver
}