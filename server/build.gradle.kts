plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("com.squareup.sqldelight")
    application
}

sqldelight {
    database("Database") {
        packageName = "com.well.server"
        dialect = "mysql"
    }
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
                        "server.sqldelight.runtimeJvm",
                        "server.sqldelight.jdbcDriver",
                        "server.sqldelight.coroutinesExtensions",
                        "server.hikariCP",
                        "server.h2database",
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
