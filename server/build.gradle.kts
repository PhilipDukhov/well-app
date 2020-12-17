plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("com.squareup.sqldelight")
    application
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
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
            libDependencies(
                ":serverModels",
                "kotlin.serializationJson",
                "ktor.server.netty",
                "ktor.server.core",
                "ktor.server.logback",
                "ktor.serialization",
                "ktor.client.serialization",
                "ktor.client.engine.cio",
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
        }
    }
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

tasks.named<JavaExec>("run") {
    io.github.cdimascio.dotenv.dotenv {
        directory = "${projectDir}/../iosApp/Well/Supporting files/"
        filename = "Shared.xcconfig"
    }.entries().forEach {
        environment(it.key, it.value)
    }
}
