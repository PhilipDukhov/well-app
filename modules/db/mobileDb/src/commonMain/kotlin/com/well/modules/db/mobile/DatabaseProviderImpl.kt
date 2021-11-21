package com.well.modules.db.mobile

import com.well.modules.atomic.AtomicLateInitRef
import com.well.modules.db.chatMessages.ChatMessages
import com.well.modules.db.chatMessages.ChatMessagesDatabase
import com.well.modules.db.chatMessages.LastReadMessages
import com.well.modules.db.chatMessages.TmpMessageIds
import com.well.modules.db.users.Users
import com.well.modules.db.users.UsersDatabase
import com.well.modules.models.User
import com.well.modules.models.chat.ChatMessage
import com.well.modules.utils.dbUtils.SerializableColumnAdapter
import com.well.modules.utils.dbUtils.SetEnumColumnAdapter
import com.well.modules.utils.viewUtils.AppContext
import com.squareup.sqldelight.EnumColumnAdapter
import io.github.aakira.napier.Napier

interface DatabaseProvider {
    fun openDatabase()
    fun clearDatabase()

    val usersDatabase: UsersDatabase
    val messagesDatabase: ChatMessagesDatabase
//    val meetingsDatabase: MeetingsDatabase
}

class DatabaseProviderImpl(appContext: AppContext) : DatabaseProvider {
    private val driverFactory = DatabaseDriverFactory(appContext)

    private var usersDatabaseContainer by AtomicLateInitRef<DatabaseContainer<UsersDatabase>>()
    override val usersDatabase get() = usersDatabaseContainer.database

    private var messagesDatabaseContainer by AtomicLateInitRef<DatabaseContainer<ChatMessagesDatabase>>()
    override val messagesDatabase get() = messagesDatabaseContainer.database

//    private var meetingsDatabaseContainer by AtomicLateInitRef<DatabaseContainer<MeetingsDatabase>>()
//    override val meetingsDatabase get() = meetingsDatabaseContainer.database

    override fun openDatabase() {
        Napier.d("open")
        usersDatabaseContainer = DatabaseContainer(
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
                        idAdapter = User.Id.ColumnAdapter,
                    )
                )
            }
        )
        messagesDatabaseContainer = DatabaseContainer(
            schema = ChatMessagesDatabase.Schema,
            driverFactory = driverFactory,
            createDatabase = { driver ->
                ChatMessagesDatabase(
                    driver,
                    ChatMessagesAdapter = ChatMessages.Adapter(
                        fromIdAdapter = User.Id.ColumnAdapter,
                        peerIdAdapter = User.Id.ColumnAdapter,
                        idAdapter = ChatMessage.Id.ColumnAdapter,
                    ),
                    LastReadMessagesAdapter = LastReadMessages.Adapter(
                        fromIdAdapter = User.Id.ColumnAdapter,
                        peerIdAdapter = User.Id.ColumnAdapter,
                        messageIdAdapter = ChatMessage.Id.ColumnAdapter,
                    ),
                    TmpMessageIdsAdapter = TmpMessageIds.Adapter(
                        idAdapter = ChatMessage.Id.ColumnAdapter
                    ),
                )
            }
        )
        Napier.d("opened $usersDatabase")
//        meetingsDatabaseContainer = DatabaseContainer(
//            name = "chatMessagesDatabase",
//            schema = MeetingsDatabase.Schema,
//            driverFactory = driverFactory,
//            createDatabase = { driver ->
//                MeetingsDatabase(
//                    driver,
//                )
//            }
//        )
    }

    override fun clearDatabase() {
        listOf(
            usersDatabaseContainer,
            messagesDatabaseContainer,
//            meetingsDatabaseContainer,
        ).forEach(DatabaseContainer<*>::clear)
    }
}