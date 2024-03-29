package com.well.server

import com.well.modules.db.server.SelectTokenByUid
import com.well.modules.models.Availability
import com.well.modules.models.CallId
import com.well.modules.models.DeviceId
import com.well.modules.models.NetworkConstants
import com.well.modules.models.Notification
import com.well.modules.models.NotificationToken
import com.well.modules.models.User
import com.well.modules.models.WebSocketMsg
import com.well.server.routing.auth.AppleOauthResponse
import com.well.server.routing.auth.appleLoginPrincipal
import com.well.server.routing.auth.build
import com.well.server.routing.auth.facebookLogin
import com.well.server.routing.auth.googleLogin
import com.well.server.routing.auth.twitterLogin
import com.well.server.routing.bookAvailability
import com.well.server.routing.deleteAvailability
import com.well.server.routing.handleTechSupportMessage
import com.well.server.routing.listBookingAvailabilities
import com.well.server.routing.listCurrentAvailabilities
import com.well.server.routing.mainWebSocket
import com.well.server.routing.putAvailability
import com.well.server.routing.upload.uploadMessageMedia
import com.well.server.routing.upload.uploadProfilePicture
import com.well.server.routing.user.delete
import com.well.server.routing.user.rate
import com.well.server.routing.user.requestBecomeExpert
import com.well.server.routing.user.setUserFavorite
import com.well.server.routing.user.updateUser
import com.well.server.routing.userHasAvailableAvailabilities
import com.well.server.utils.Services
import com.well.server.utils.ForbiddenException
import com.well.server.utils.authUid
import com.well.server.utils.configProperty
import com.well.server.utils.createPrincipal
import com.well.server.utils.executeQueryAndPrettify
import com.well.server.utils.notifications.sendNotification
import com.well.server.utils.sendEmail
import com.auth0.jwk.JwkProviderBuilder
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.forms.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.util.*
import io.ktor.websocket.*
import org.slf4j.event.Level
import java.net.URL
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val dbConfig = environment.config.config("database")
    val username = dbConfig.property("username").getString()
    try {
        initializedModule(Services(this))
    } catch (e: Exception) {
        println("initialization failed: ${e.stackTraceToString()}")
        if (username == "test") {
            // e.g. local machine
            throw e
        }
        try {
            sendEmail(
                destination = "philip.dukhov@gmail.com",
                subject = "W.E.L.L. Server failed to start ${e.localizedMessage}",
                body = e.stackTraceToString(),
            )
        } catch (e: Exception) {
            println("sendEmail failed $e")
        }
        routing {
            route("*") {
                handle {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}

private object AdminPrincipal : Principal

fun Application.initializedModule(services: Services) {
    install(CallLogging) {
        level = Level.INFO
        filter { call ->
            call.request.path()
                .startsWith("/")
        }
    }

    install(ContentNegotiation) {
        json()
    }

    install(StatusPages) {
        exception<ForbiddenException> {
            call.respond(HttpStatusCode.Forbidden)
        }
        exception<Exception> { cause ->
            println("StatusPages failed $cause")
            println(cause.stackTraceToString())
            call.respond(HttpStatusCode.InternalServerError, cause.toString())
        }
    }

    val twitterLoginProvider = OAuthServerSettings.OAuth1aServerSettings(
        name = "twitter",
        requestTokenUrl = "https://api.twitter.com/oauth/request_token",
        authorizeUrl = "https://api.twitter.com/oauth/authorize",
        accessTokenUrl = "https://api.twitter.com/oauth/access_token",

        consumerKey = environment.configProperty("social.twitter.apiKey"),
        consumerSecret = environment.configProperty("social.twitter.apiSecret"),
    )

    install(Authentication) {
        basic(AuthName.Admin) {
            val adminPassword = environment.config.config("social").property("adminPassword").getString()
            validate {
                if (adminPassword.isNotBlank() && it.password == adminPassword) {
                    AdminPrincipal
                } else {
                    null
                }
            }
        }
        jwt(AuthName.Main) {
            verifier(services.jwtConfig.verifier)
            realm = services.jwtConfig.issuer
            validate {
                it.payload.createPrincipal(services)
            }
        }
        oauth(AuthName.Twitter) {
            client = services.client
            providerLookup = {
                twitterLoginProvider
            }
            urlProvider = {
                NetworkConstants.oauthCallbackPath()
            }
        }
        jwt(AuthName.Apple) {
            verifier(
                JwkProviderBuilder(URL("https://appleid.apple.com/auth/keys"))
                    .cached(10, 24, TimeUnit.HOURS)
                    .rateLimited(10, 1, TimeUnit.MINUTES)
                    .build(),
                "https://appleid.apple.com"
            )
            validate { credentials ->
                log.debug("$credentials")
                log.debug("${credentials.payload}")
                JWTPrincipal(credentials.payload)
            }
        }
    }

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

//    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
//    install(MicrometerMetrics) {
//        registry = appMicrometerRegistry
//        meterBinders = listOf(
//            JvmMemoryMetrics(),
//            JvmGcMetrics(),
//            ProcessorMetrics()
//        )
//        distributionStatisticConfig = DistributionStatisticConfig.Builder()
//            .percentilesHistogram(true)
//            .maximumExpectedValue(Duration.ofSeconds(20).toNanos())
//            .sla(
//                Duration.ofMillis(100).toNanos(),
//                Duration.ofMillis(500).toNanos()
//            )
//            .build()
//    }

    routing {
        route("login") {
            post("facebook") { facebookLogin(services) }
            post("google") { googleLogin(services) }
            post("appleCallback") {
                val params = call.receive<String>()
                    .parseUrlEncodedParameters()
                val error = params["error"]
                if (error != null) {
                    call.respond(HttpStatusCode.BadRequest, error)
                    return@post
                }
                val code = params["code"]!!
//                val code = params["id_token"]!!
                println("$code $params ${call.request.headers.flattenEntries()}")

                val appleOauthResponse = application.environment.run {
                    services.client.config {
                        install(Logging) {
                            level = LogLevel.ALL
                        }
                    }.submitForm<AppleOauthResponse>(
                        url = "https://appleid.apple.com/auth/token",
                        formParameters = Parameters.build(
                            listOf(
                                "grant_type" to "authorization_code",
                                "code" to code,
                                "redirect_uri" to "https://well-env-1.eba-yyqqrxsi.us-east-2.elasticbeanstalk.com/login/appleCallback",
                                "client_id" to configProperty("social.apple.clientId"),
                                "client_secret" to services.jwtConfig.createAppleAuthToken(
                                    keyId = configProperty("social.apple.serviceKeyId"),
                                    privateKeyString = String(
                                        Base64.getDecoder()
                                            .decode(environment.configProperty("social.apple.privateKey"))
                                    ),
                                    teamId = configProperty("social.apple.teamId")
                                ),
                            ).map { pair -> pair.second.let { pair.first to it } }
                        ),
                        encodeInQuery = false,
//                        block = { header("user-agent", "cheer-with-me") }
                    )
                }

                println("RESPONSE $appleOauthResponse")
                call.respond(appleOauthResponse)
            }
            authenticate(AuthName.Apple) {
                post("apple") {
                    appleLoginPrincipal(services)
                }
            }
            authenticate(AuthName.Twitter) {
                post("twitter") {
                    twitterLogin(services)
                }
            }
        }
        post("twitter_oauth_callback") { twitterLogin(services) }
//        post("email") { sendEmail(dependencies) }
//        post("sms") { sendSms(dependencies) }

        authenticate(AuthName.Admin) {
            post("executeQuerySql") {
                val request = call.receive<String>()
                println("\nexecuteSql ${call.authUid} $request")
                val result = services.dbDriver.executeQueryAndPrettify(request)
                    .also(::print)
                println("")
                call.respond(HttpStatusCode.OK, result)
            }
            post("executeSql") {
                val request = call.receive<String>()
                println("\nexecuteSql ${call.authUid} $request")
                val result = services.dbDriver.execute(
                    null,
                    request,
                    0
                )
                call.respond(HttpStatusCode.OK, result)
            }

            post("sendTestNotification") {
                val res = sendNotification(
                    tokenInfo = SelectTokenByUid(
                        token = NotificationToken.Apns(
                            notificationToken = null,
                            voipToken = call.receive(),
                            bundleId = "com.well.app.debug"
                        ),
                        deviceId = DeviceId(value = "")
                    ),
                    notification = Notification.Voip.IncomingCall(
                        webSocketMsg = WebSocketMsg.Back.IncomingCall(
                            callId = CallId.new(),
                            user = services.getCurrentUser(id = User.Id(6)),
                            hasVideo = false,
                        ),
                        totalUnreadCount = 0,
                    ),
                    services = services,
                )
                call.respond(HttpStatusCode.OK, res)
            }
//            post("testNotification") {
////            sendFcmNotification(dependencies, call.receive())
//                call.respond(HttpStatusCode.OK)
//            }
//            post("testAppleNotification") {
//                sendVoipApnsNotification(call.receive(), dependencies = dependencies)
//                call.respond(HttpStatusCode.OK)
//            }
        }
        authenticate(AuthName.Main) {
            webSocket(path = "mainWebSocket/{deviceId}") {
                mainWebSocket(services, DeviceId(call.parameters["deviceId"]!!))
            }
            post("uploadMessageMedia") { uploadMessageMedia(services) }
            post("techSupportMessage") { handleTechSupportMessage(services) }
            route("user") {
                put { updateUser(services) }
                post("uploadProfilePicture") { uploadProfilePicture(services) }
                post("rate") {
                    rate(services)
                }
                post("setFavorite") {
                    setUserFavorite(services)
                }
                post("requestBecomeExpert") {
                    requestBecomeExpert(services)
                }
                delete {
                    delete(services)
                }
            }
            route("availabilities") {
                get("listCurrent") {
                    listCurrentAvailabilities(services)
                }
                get("listByUser/{id}") {
                    listBookingAvailabilities(call.idParameter(User::Id), services)
                }
                get("userHasAvailable/{id}") {
                    userHasAvailableAvailabilities(call.idParameter(User::Id), services)
                }
                put {
                    putAvailability(services)
                }
                delete("{id}") {
                    deleteAvailability(call.idParameter(Availability::Id), services)
                }
                post("book") {
                    bookAvailability(services)
                }
            }
        }
    }
}

private fun <Id> ApplicationCall.idParameter(builder: (Long) -> Id): Id =
    builder(parameters["id"]!!.toLong())
