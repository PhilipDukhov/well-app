package com.well.modules.db.users

import com.well.modules.models.User
import com.well.modules.utils.dbUtils.SerializableColumnAdapter
import com.well.modules.utils.dbUtils.SetEnumColumnAdapter
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.db.SqlDriver

fun UsersDatabase.Companion.create(driver: SqlDriver) = UsersDatabase(
    driver,
    UsersAdapter = Users.Adapter(
        typeAdapter = EnumColumnAdapter(),
        credentialsAdapter = EnumColumnAdapter(),
        academicRankAdapter = EnumColumnAdapter(),
        languagesAdapter = SetEnumColumnAdapter(),
        skillsAdapter = SetEnumColumnAdapter(),
        reviewInfoAdapter = SerializableColumnAdapter(),
        idAdapter = User.Id.ColumnAdapter,
    )
)