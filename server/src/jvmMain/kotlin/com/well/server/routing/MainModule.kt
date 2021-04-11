package com.well.server.routing

import com.well.server.routing.auth.appleLogin
import com.well.server.routing.auth.facebookLogin
import com.well.server.routing.auth.googleLogin
import com.well.server.routing.auth.sendEmail
import com.well.server.routing.userEditing.updateUser
import com.well.server.routing.userEditing.uploadUserProfile
import com.well.server.utils.Dependencies
import com.well.server.utils.createPrincipal
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.metrics.micrometer.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.websocket.*
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
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
            println("StatusPages failed $cause")
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

    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
        meterBinders = listOf(
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics()
        )
        distributionStatisticConfig = DistributionStatisticConfig.Builder()
            .percentilesHistogram(true)
            .maximumExpectedValue(Duration.ofSeconds(20).toNanos())
            .sla(
                Duration.ofMillis(100).toNanos(),
                Duration.ofMillis(500).toNanos()
            )
            .build()
    }

    routing {
        post("/facebookLogin") { facebookLogin(dependencies) }
        post("/googleLogin") { googleLogin(dependencies) }
        post("/appleLogin") { appleLogin(dependencies) }
        put("/user") { updateUser(dependencies) }
        post("/uploadUserProfile") { uploadUserProfile(dependencies) }
        get("/metrics") {
            call.respond(appMicrometerRegistry.scrape())
        }

        post("/email") { sendEmail(dependencies) }

        authenticate {
            webSocket(path = "/mainWebSocket") {
                mainWebSocket(dependencies)
            }
        }
    }
}

