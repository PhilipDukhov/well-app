package com.well.modules.db.mobile

import com.well.modules.db.mobile.helper.DatabaseDriverFactory
import com.well.modules.utils.viewUtils.SystemContext
import com.well.modules.utils.viewUtils.platform.Platform
import com.well.modules.utils.viewUtils.platform.isLocalServer
import com.squareup.sqldelight.db.SqlDriver

internal class DatabaseContainer<T>(
    schema: SqlDriver.Schema,
    val driverFactory: DatabaseDriverFactory,
    createDatabase: (SqlDriver) -> T,
) {
    private val fileName: String
    init {
        val name = schema::class.qualifiedName!!.split(".").takeLast(2).first()
        fileName = if (Platform.isLocalServer) "${name}.Debug.db" else "${name}Prod.db"
    }
    private val driver = driverFactory.createDriver(fileName, schema)
    val database = createDatabase(driver)

    fun clear(systemContext: SystemContext) {
        driver.close()
        driverFactory.deleteDatabase(fileName, systemContext)
    }
}