package com.well.modules.db

import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import com.well.modules.dbHelper.SerializableColumnAdapter
import com.well.modules.dbHelper.SetEnumColumnAdapter
import com.well.modules.dbHelper.migrateIfNeeded
import com.well.modules.utils.AppContext
import com.well.modules.utils.platform.Platform
import com.well.modules.utils.platform.isDebug
import com.well.modules.utils.platform.prodTesting

class DatabaseManager(appContext: AppContext) {
    private val driverFactory = DatabaseDriverFactory(appContext)
    private lateinit var driver: SqlDriver
    lateinit var database: Database
        private set

    init {
        open()
    }

    private fun open() {
        driver = driverFactory
            .createDriver(if (Platform.isDebug) "debugDatabase.db" else "prodDatabase.db")
        database = Database(
            driver,
            UsersAdapter = Users.Adapter(
                typeAdapter = EnumColumnAdapter(),
                credentialsAdapter = EnumColumnAdapter(),
                academicRankAdapter = EnumColumnAdapter(),
                languagesAdapter = SetEnumColumnAdapter(),
                skillsAdapter = SetEnumColumnAdapter(),
                ratingInfoAdapter = SerializableColumnAdapter(),
            )
        )
    }

    fun close() {
        driver.close()
    }
}