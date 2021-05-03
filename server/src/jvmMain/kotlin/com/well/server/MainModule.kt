package com.well.server

import com.auth0.jwk.JwkProviderBuilder
import com.well.modules.models.Constants
import com.well.server.routing.auth.appleLogin
import com.well.server.routing.auth.facebookLogin
import com.well.server.routing.auth.googleLogin
import com.well.server.routing.auth.sendEmail
import com.well.server.routing.auth.sendSms
import com.well.server.routing.auth.twitterLogin
import com.well.server.routing.mainWebSocket
import com.well.server.routing.user.filterUsers
import com.well.server.routing.user.setUserFavorite
import com.well.server.routing.user.updateUser
import com.well.server.routing.user.uploadUserProfile
import com.well.server.utils.Dependencies
import com.well.server.utils.configProperty
import com.well.server.utils.createPrincipal
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.client.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.locations.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.util.*
import io.ktor.websocket.*
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.openssl.PEMException
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.slf4j.event.Level
import java.io.File
import java.io.StringReader
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

    install(Locations)
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
//                val params = call.receive<String>()
//                    .parseUrlEncodedParameters()
//                val error = params["error"]
//                if (error != null) {
//                    call.respond(HttpStatusCode.BadRequest, error)
//                    return@post
//                }
//                val code = params["code"]!!
                val code = call.receive<String>()
//                println("$code $params ${call.request.headers.flattenEntries()}")

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
                            ).mapNotNull { pair -> pair.second?.let { pair.first to it } }
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
//                    val appleUserSignInRequest = call.receive<AppleUserSignInRequest>()
//                    call.application.log.debug("Code ${appleUserSignInRequest.code}")
//                    val appleOauthResponse = application.environment.run {
//                        Oauth2Service(dependencies.client).authenticate(
//                            "https://appleid.apple.com/auth/token", Oauth2Parameters(
//                                grantType = "authorization_code",
//                                code = appleUserSignInRequest.code,
//                                clientId = configProperty("social.apple.clientId"),
//                                clientSecret = dependencies.jwtConfig.createAppleAuthToken(
//                                    keyId = configProperty("social.apple.keyId"),
//                                    teamId = configProperty("social.apple.teamId")
//                                ),
//                                redirectUri = null
//                            )
//                        )
//                    }
//
//                    val principal = call.authentication.principal<JWTPrincipal>()!!
//
//                    println("RESPONSE $appleOauthResponse $principal")
//                    call.respond(appleOauthResponse)
                }
            }
            authenticate(AuthName.Twitter) {
                post("/twitter") {
                    twitterLogin(dependencies)
                }
            }
        }
        post("/twitter_oauth_callback") { appleLogin(dependencies) }

        put("/user") { updateUser(dependencies) }
        post("/uploadUserProfile") { uploadUserProfile(dependencies) }
//        get("/metrics") {
//            call.respond(appMicrometerRegistry.scrape())
//        }

        post("/email") { sendEmail(dependencies) }
        post("/sms") { sendSms(dependencies) }

        authenticate(AuthName.Main) {
            webSocket(path = "/mainWebSocket") {
                mainWebSocket(dependencies)
            }
            route("/user") {
                post("/filteredList") {
                    filterUsers(dependencies)
                }
                post("setFavorite") {
                    setUserFavorite(dependencies)
                }
            }
        }
    }
}

val OAuthAccessTokenResponse.extraParameters: Parameters
    get() = when (this) {
        is OAuthAccessTokenResponse.OAuth1a -> extraParameters
        is OAuthAccessTokenResponse.OAuth2 -> extraParameters
    }

data class AppleUserSignInRequest(
    val code: String,
    val nick: String
)

data class Oauth2Parameters(
    val grantType: String,
    val code: String,
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String?
)

class Oauth2Service(val client: HttpClient) {
    suspend inline fun authenticate(
        tokenUrl: String,
        parameters: Oauth2Parameters
    ): AppleOauthResponse {
        val data = Parameters.build {
            append("grant_type", parameters.grantType)
            append("code", parameters.code)
            parameters.redirectUri?.apply { append("redirect_uri", parameters.redirectUri) }
            append("client_id", parameters.clientId)
            append(
                "client_secret", parameters.clientSecret
            )
        }

        println("Sending form with data: $data")
        val body = client.submitForm<AppleOauthResponse>(
            url = tokenUrl,
            formParameters = data,
            encodeInQuery = false,
//            block = { header("user-agent", "cheer-with-me") }
        )

        println(body)
        return body
    }
}

data class AppleOauthResponse(
    val access_token: String,
    val expires_in: Long,
    val id_token: String,
    val refresh_token: String,
    val token_type: String
)

fun Parameters.Companion.build(list: List<Pair<String, String>>) =
    build {
        list.forEach {
            append(it.first, it.second)
        }
    }