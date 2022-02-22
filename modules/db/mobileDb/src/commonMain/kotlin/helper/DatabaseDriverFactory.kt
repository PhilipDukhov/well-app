package com.well.modules.db.mobile.helper

import com.well.modules.utils.viewUtils.ApplicationContext
import com.well.modules.utils.viewUtils.SystemContext
import com.squareup.sqldelight.db.SqlDriver

internal expect class DatabaseDriverFactory(applicationContext: ApplicationContext) {
    fun createDriver(filename: String, schema: SqlDriver.Schema): SqlDriver
    fun deleteDatabase(filename: String, systemContext: SystemContext)
}