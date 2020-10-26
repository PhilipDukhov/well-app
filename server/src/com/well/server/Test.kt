package com.well.server

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.features.*
import org.slf4j.event.*
import io.ktor.routing.*
import io.ktor.auth.*
import io.ktor.http.ContentType.Text.Plain

import com.well.serverModels.Point
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(Authentication) {
        basic("myBasicAuth") {
            realm = "Ktor Server"
            validate { if (it.name == "asd" && it.password == "asd") UserIdPrincipal(it.name) else null }
        }
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD! " + Json.encodeToString(Point(x = 100F, y = 20F)), contentType = Plain)
        }

        authenticate("myBasicAuth") {
            get("/protected/route/basic") {
                val principal = call.principal<UserIdPrincipal>()!!
                call.respondText("Hello ${principal.name}")
            }
        }
    }
}

