import io.github.cdimascio.dotenv.dotenv

plugins {
    kotlin("multiplatform")
    application
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
    @Suppress("DEPRECATION")
    mainClassName = mainClass.get()
}

kotlin {
    jvm {
        withJava()
    }
    sourceSets {
        optIns(optIns = setOf(OptIn.Coroutines))
        val jvmMain by getting {
            libDependencies(
                ":modules:utils:dbUtils",
                ":modules:db:chatMessagesDb",
                ":modules:db:serverDb",
                ":modules:utils:flowUtils",
                ":modules:utils:ktorUtils",
                ":modules:models",
                "server.*",
                "kotlin.coroutines.core",
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
