package com.well.server.utils

import com.well.modules.models.User
import com.well.modules.utils.flowUtils.MutableMapFlow
import com.well.modules.utils.ktorUtils.createBaseHttpClient
import com.well.server.routing.UserSession
import io.ktor.application.*
import java.util.*

class Dependencies(app: Application) {
    val environment = app.environment
    val database = initialiseDatabase(app)
    val jwtConfig = JwtConfig(
        environment.configProperty("jwt.accessTokenSecret"),
    )
    val awsManager: AwsManager by lazy {
        AwsManager(
            environment.configProperty("aws.accessKeyId"),
            environment.configProperty("aws.secretAccessKey"),
            environment.configProperty("aws.bucketName"),
        )
    }
    val connectedUserSessionsFlow = MutableMapFlow<User.Id, UserSession>()
    val callInfos: MutableList<CallInfo> = Collections.synchronizedList(mutableListOf<CallInfo>())
    val client = createBaseHttpClient()

    fun awsProfileImagePath(
        uid: User.Id,
        ext: String,
    ) = "profilePictures/$uid-${UUID.randomUUID()}.$ext"

    fun getCurrentUser(id: User.Id) = getUser(uid = id, currentUid = id)

    fun getUser(
        uid: User.Id,
        currentUid: User.Id,
    ) = database
        .usersQueries
        .getById(uid)
        .executeAsOne()
        .toUser(
            currentUid = currentUid,
            database = database,
        )
}