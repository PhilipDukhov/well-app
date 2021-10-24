package com.well.server

import com.well.modules.utils.Constants
import com.well.server.routing.auth.AppleOauthResponse
import com.well.server.routing.auth.appleLoginPrincipal
import com.well.server.routing.auth.build
import com.well.server.routing.auth.facebookLogin
import com.well.server.routing.auth.googleLogin
import com.well.server.routing.auth.sendEmail
import com.well.server.routing.auth.sendSms
import com.well.server.routing.auth.twitterLogin
import com.well.server.routing.mainWebSocket
import com.well.server.routing.upload.uploadMessageMedia
import com.well.server.routing.upload.uploadProfilePicture
import com.well.server.routing.user.rate
import com.well.server.routing.user.requestBecomeExpert
import com.well.server.routing.user.setUserFavorite
import com.well.server.routing.user.updateUser
import com.well.server.utils.Dependencies
import com.well.server.utils.configProperty
import com.well.server.utils.createPrincipal
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
    val dependencies = Dependencies(this)

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
        exception<Throwable> { cause ->
            println("StatusPages failed $cause")
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
        jwt(AuthName.Main) {
            verifier(dependencies.jwtConfig.verifier)
            realm = dependencies.jwtConfig.issuer
            validate { it.payload.createPrincipal(dependencies) }
        }
        oauth(AuthName.Twitter) {
            client = dependencies.client
            providerLookup = {
                twitterLoginProvider
            }
            urlProvider = {
                Constants.oauthCallbackPath()
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
            post("facebook") { facebookLogin(dependencies) }
            post("google") { googleLogin(dependencies) }
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
                    dependencies.client.config {
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
                                "client_id" to "com.well.server.debug",//configProperty("social.apple.clientId"),
                                "client_secret" to dependencies.jwtConfig.createAppleAuthToken(
                                    keyId = configProperty("social.apple.keyId"),
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
                    appleLoginPrincipal(dependencies)
                }
            }
            authenticate(AuthName.Twitter) {
                post("twitter") {
                    twitterLogin(dependencies)
                }
            }
        }
        post("twitter_oauth_callback") { twitterLogin(dependencies) }

//        get("/metrics") {
//            call.respond(appMicrometerRegistry.scrape())
//        }

        post("email") { sendEmail(dependencies) }
        post("sms") { sendSms(dependencies) }

        authenticate(AuthName.Main) {
            webSocket(path = "mainWebSocket") {
                mainWebSocket(dependencies)
            }
            post("uploadMessageMedia") { uploadMessageMedia(dependencies) }
            route("user") {
                put { updateUser(dependencies) }
                post("uploadProfilePicture") { uploadProfilePicture(dependencies) }
                post("rate") {
                    rate(dependencies)
                }
                post("setFavorite") {
                    setUserFavorite(dependencies)
                }
                post("requestBecomeExpert") {
                    requestBecomeExpert(dependencies)
                }
            }
        }
    }
}