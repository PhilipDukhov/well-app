package com.well.modules.db.mobile

import com.squareup.sqldelight.db.SqlDriver
import com.well.modules.utils.platform.Platform
import com.well.modules.utils.platform.isDebug
import co.touchlab.sqliter.DatabaseFileContext

internal class DatabaseContainer<T> (
    name: String,
    schema: SqlDriver.Schema,
    driverFactory: DatabaseDriverFactory,
    createDatabase: (SqlDriver) -> T
) {
    private val fileName = if (Platform.isDebug) "${name}Debug.db" else "${name}Prod.db"
    private val driver = driverFactory.createDriver(fileName, schema)
    val database = createDatabase(driver)

    fun clear() {
        driver.close()
        DatabaseFileContext.deleteDatabase(fileName)
    }
}