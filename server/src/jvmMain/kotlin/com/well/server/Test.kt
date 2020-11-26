package com.well.server


import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.apache.v2.ApacheHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.well.server.utils.JwtConfig
import com.well.server.utils.getPrimitiveContent
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import org.slf4j.event.*
import java.util.*

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ContentNegotiation) {
        json()
    }

    install(StatusPages) {
        exception<Throwable> { cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.toString())
        }
    }

    val jwtConfig = JwtConfig(environment.config)
    install(Authentication) {
        jwt {
            verifier(jwtConfig.verifier)
            realm = jwtConfig.issuer
            validate {
                it.payload.run {
                    if (claims.contains("id")) JWTPrincipal(this) else null
                }
            }
        }
    }

    val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
        HttpResponseValidator {
            validateResponse { response ->
                val status = response.status.value
                if (status < 300) return@validateResponse

                val responseString = String(response.readBytes())
                val exceptionResponse = Json.parseToJsonElement(responseString)

                when (status) {
                    in 300..399 -> throw Throwable(exceptionResponse.toString())
                    in 400..499 -> throw Throwable(exceptionResponse.toString())
                    in 500..599 -> throw Throwable(exceptionResponse.toString())
                    else -> throw Throwable(exceptionResponse.toString())
                }
            }
        }
    }

    val database = initialiseDatabase(this)

    routing {
        post("/facebook") {
            val token = call.receive<String>()
            println(token)
            val appId = environment.config.property("facebook.appId").getString()
            val appSecret = environment.config.property("facebook.appSecret").getString()
            val appAccessToken = client.get<JsonElement>(
                "https://graph.facebook.com/oauth/access_token?client_id=$appId&client_secret=$appSecret&grant_type=client_credentials"
            ).jsonObject
                .getPrimitiveContent("access_token")
            val facebookUserId = client.get<JsonElement>(
                "https://graph.facebook.com/debug_token?input_token=$token&access_token=$appAccessToken"
            ).jsonObject
                .getValue("data")
                .jsonObject.getPrimitiveContent("user_id")

            val fields = object {
                val id = "id"
                val firstName = "first_name"
                val lastName = "last_name"
            }
            val userInfo: JsonElement = client.get(
                "https://graph.facebook.com/v9.0/$facebookUserId?fields=${fields.firstName},${fields.lastName},${fields.id}&access_token=$appAccessToken"
            )

            val userId = userInfo.jsonObject.let { json ->
                val facebookId = json.getPrimitiveContent(fields.id)
                database.userQueries.run {
                    getByFacebookId(facebookId)
                        .executeAsOneOrNull()
                        ?: run {
                            println("user created")
                            insertFacebook(
                                json.getPrimitiveContent(fields.firstName),
                                json.getPrimitiveContent(fields.lastName),
                                facebookId
                            )
                            lastInsertId()
                                .executeAsOne()
                                .toInt()
                        }
                }
            }

            call.respond(HttpStatusCode.Created, mapOf("token" to jwtConfig.makeToken(userId)))
        }

        post("/google") {
            val clientId = environment.config.property("google.clientId").getString()
            val userId = withContext(Dispatchers.IO) {
                @Suppress("BlockingMethodInNonBlockingContext")
                GoogleIdTokenVerifier.Builder(
                    ApacheHttpTransport(),
                    JacksonFactory()
                ).setAudience(listOf(clientId))
                    .build()
                    .verify(call.receive<String>())
            }.payload.run {
                val googleId = subject

                database.userQueries.run {
                    getByGoogleId(googleId)
                        .executeAsOneOrNull()
                        ?: run {
                            insertGoogle(
                                getValue("given_name") as String,
                                getValue("family_name") as String,
                                googleId
                            )
                            lastInsertId()
                                .executeAsOne()
                                .toInt()
                        }
                }
            }
            call.respond(HttpStatusCode.Created, mapOf("token" to jwtConfig.makeToken(userId)))
        }

        authenticate {
            get("/") {
                val principal = call.principal<JWTPrincipal>()!!
                val id = principal.payload.claims["id"]!!.asInt()
                val userName = database
                    .userQueries
                    .getById(id)
                    .executeAsOne()
                    .run {
                        "$id $firstName $lastName ${String.format("%.4f", createdDate)}"
                    }
                call.respond(userName)
            }
        }
    }
}