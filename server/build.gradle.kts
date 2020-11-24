plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    application
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

kotlin {
    jvm {
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
//                implementation(project(":serverModels"))
                extra.libsAt(
                    listOf(
                        "kotlin.serializationJson",
                        "ktor.server.netty",
                        "ktor.server.core",
                        "ktor.server.logback",
                        "ktor.serialization",
                        "ktor.client.serialization",
                        "ktor.client.cio",
                        "ktor.auth",
                        "ktor.metrics",
                        "ktor.websockets",
                        "kotlin.serializationJson",
                        "ktor.authJwt",
                        "ktor.serialization",
                        "google.apiClient",
                        "google.httpClientApacheV2",
//                        "klock",
                        "kotlin.stdLib"
                    )
                ).forEach { implementation(it) }
            }
        }
    }
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

tasks.withType<Jar> {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to application.mainClass.get()
            )
        )
    }
}
