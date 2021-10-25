package com.well.modules.db.mobile

import com.well.modules.db.chatMessages.ChatMessagesDatabase
import com.squareup.sqldelight.EnumColumnAdapter
import com.well.modules.db.helper.SerializableColumnAdapter
import com.well.modules.db.helper.SetEnumColumnAdapter
import com.well.modules.db.users.Users
import com.well.modules.db.users.UsersDatabase
import com.well.modules.utils.viewUtils.AppContext
import com.well.modules.atomic.AtomicLateInitRef

class DatabaseManager(appContext: AppContext) {
    private val driverFactory = DatabaseDriverFactory(appContext)

    private var usersDatabaseContainer by AtomicLateInitRef<DatabaseContainer<UsersDatabase>>()
    val usersDatabase: UsersDatabase
        get() = usersDatabaseContainer.database

    private var messagesDatabaseContainer by AtomicLateInitRef<DatabaseContainer<ChatMessagesDatabase>>()
    val messagesDatabase: ChatMessagesDatabase
        get() = messagesDatabaseContainer.database

    fun open() {
        usersDatabaseContainer = DatabaseContainer(
            name = "usersDatabase",
            schema = UsersDatabase.Schema,
            driverFactory = driverFactory,
            createDatabase = { driver ->
                UsersDatabase(
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
        )
        messagesDatabaseContainer = DatabaseContainer(
            name = "chatMessagesDatabase",
            schema = ChatMessagesDatabase.Schema,
            driverFactory = driverFactory,
            createDatabase = { driver ->
                ChatMessagesDatabase(
                    driver,
                )
            }
        )
    }

    fun clear() {
        listOf(
            usersDatabaseContainer,
            messagesDatabaseContainer,
        ).forEach(DatabaseContainer<*>::clear)
    }
}