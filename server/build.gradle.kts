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
        withJava()
    }
    sourceSets {
        usePredefinedExperimentalAnnotations("KtorExperimentalLocationsAPI", "FlowPreview")
        val commonMain by getting {
            libDependencies(
                ":modules:models",
                ":modules:dbHelper",
            )
        }

        val jvmMain by getting {
            libDependencies(
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
                "sqldelight.coroutinesExtensions",
                "sqldelight.jdbcDriver",
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
    classpath += objects.fileCollection().from(
        tasks.named("compileKotlinJvm"),
        configurations.named("jvmRuntimeClasspath")
    )
}

tasks.named<AbstractCopyTask>("jvmProcessResources") {
    duplicatesStrategy = DuplicatesStrategy.WARN
}