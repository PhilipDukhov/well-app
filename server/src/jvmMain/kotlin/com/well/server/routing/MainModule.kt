package com.well.server.routing

import com.well.server.routing.auth.*
import com.well.server.utils.Dependencies
import com.well.server.utils.authUserId
import com.well.server.utils.createPrincipal
import com.well.server.utils.toUser
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.websocket.*
import kotlinx.serialization.json.JsonElement
import org.slf4j.event.Level
import java.time.Duration

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
            println(cause)
            call.respond(HttpStatusCode.InternalServerError, cause.toString())
        }
    }
    install(Authentication) {
        jwt {
            verifier(dependencies.jwtConfig.verifier)
            realm = dependencies.jwtConfig.issuer
            validate { it.payload.createPrincipal(dependencies) }
        }
    }

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        post("/facebookLogin") { facebookLogin(dependencies) }
        post("/googleLogin") { googleLogin(dependencies) }
        post("/testLogin") { testLogin(dependencies) }

        authenticate {
            webSocket(path = "/mainWebSocket") {
                mainWebSocket(dependencies)
            }
        }
    }
}

