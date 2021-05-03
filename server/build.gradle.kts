import io.github.cdimascio.dotenv.dotenv

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.squareup.sqldelight")
    application
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
    @Suppress("DEPRECATION")
    mainClassName = mainClass.get()
}

sqldelight {
    database("Database") {
        packageName = "com.well.server"
        dialect = "mysql"
    }
}

kotlin {
    jvm {
        val kotlinVersion = (extra["Versions"] as Map<*, *>)["kotlin"] as String
        if (kotlinVersion.startsWith("1.5")) {
            withJava()
        }
    }
    sourceSets {
        usePredefinedExperimentalAnnotations("KtorExperimentalLocationsAPI")
        val jvmMain by getting {
            libDependencies(
                ":modules:models",
                "server.*",
                "ktor.server.*",
                "ktor.serialization",
                "ktor.client.serialization",
                "ktor.client.logging",
                "ktor.client.engine.cio",
                "ktor.auth",
                "ktor.authJwt",
                "ktor.metrics",
                "ktor.websockets",
                "google.apiClient",
                "google.httpClientApacheV2",
                "logback",
                "kotlin.serializationJson",
                "kotlin.stdLib",
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
    dotenv {
        directory = "${projectDir}/../iosApp/Well/Supporting files/"
        filename = "Shared.xcconfig"
    }.entries().forEach {
        environment(it.key, it.value)
        System.setProperty(it.value, it.key)
    }
}

tasks.named<AbstractCopyTask>("jvmProcessResources") {
    duplicatesStrategy = DuplicatesStrategy.WARN
}