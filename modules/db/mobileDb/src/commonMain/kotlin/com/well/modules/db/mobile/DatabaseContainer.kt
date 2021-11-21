package com.well.modules.db.mobile

import com.well.modules.utils.viewUtils.platform.Platform
import com.well.modules.utils.viewUtils.platform.isDebug
import com.squareup.sqldelight.db.SqlDriver

internal class DatabaseContainer<T> (
    schema: SqlDriver.Schema,
    val driverFactory: DatabaseDriverFactory,
    createDatabase: (SqlDriver) -> T,
) {
    private val fileName: String
    init {
        val name = schema::class.qualifiedName!!.split(".").takeLast(2).first()
        fileName = if (Platform.isDebug) "${name}.Debug.db" else "${name}Prod.db"
    }
    private val driver = driverFactory.createDriver(fileName, schema)
    val database = createDatabase(driver)

    fun clear() {
        driver.close()
        driverFactory.deleteDatabase(fileName)
    }
}