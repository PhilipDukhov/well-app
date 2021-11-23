package com.well.modules.db.mobile

import com.well.modules.atomic.AtomicLateInitRef
import com.well.modules.db.chatMessages.ChatMessagesDatabase
import com.well.modules.db.chatMessages.create
import com.well.modules.db.meetings.MeetingsDatabase
import com.well.modules.db.meetings.MeetingsQueries
import com.well.modules.db.meetings.create
import com.well.modules.db.mobile.helper.DatabaseDriverFactory
import com.well.modules.db.users.UsersDatabase
import com.well.modules.db.users.UsersQueries
import com.well.modules.db.users.create
import com.well.modules.utils.viewUtils.AppContext
import io.github.aakira.napier.Napier

interface DatabaseProvider {
    fun openDatabase()
    fun clearDatabase()

    val usersQueries: UsersQueries
    val messagesDatabase: ChatMessagesDatabase
    val meetingsQueries: MeetingsQueries
}

fun createDatabaseProvider(appContext: AppContext): DatabaseProvider =
    DatabaseProviderImpl(appContext)

internal class DatabaseProviderImpl(appContext: AppContext) : DatabaseProvider {
    private val driverFactory = DatabaseDriverFactory(appContext)

    private var usersDatabaseContainer by AtomicLateInitRef<DatabaseContainer<UsersDatabase>>()
    override val usersQueries get() = usersDatabaseContainer.database.usersQueries

    private var messagesDatabaseContainer by AtomicLateInitRef<DatabaseContainer<ChatMessagesDatabase>>()
    override val messagesDatabase get() = messagesDatabaseContainer.database

    private var meetingsDatabaseContainer by AtomicLateInitRef<DatabaseContainer<MeetingsDatabase>>()
    override val meetingsQueries get() = meetingsDatabaseContainer.database.meetingsQueries

    override fun openDatabase() {
        usersDatabaseContainer = DatabaseContainer(
            schema = UsersDatabase.Schema,
            driverFactory = driverFactory,
            createDatabase = UsersDatabase::create
        )
        messagesDatabaseContainer = DatabaseContainer(
            schema = ChatMessagesDatabase.Schema,
            driverFactory = driverFactory,
            createDatabase = ChatMessagesDatabase::create
        )
        meetingsDatabaseContainer = DatabaseContainer(
            schema = MeetingsDatabase.Schema,
            driverFactory = driverFactory,
            createDatabase = MeetingsDatabase::create
        )
    }

    override fun clearDatabase() {
        listOf(
            usersDatabaseContainer,
            messagesDatabaseContainer,
            meetingsDatabaseContainer,
        ).forEach(DatabaseContainer<*>::clear)
    }
}