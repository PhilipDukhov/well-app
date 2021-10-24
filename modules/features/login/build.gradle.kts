import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    if (withAndroid) {
        id("com.android.library")
    }
    id("com.codingfeline.buildkonfig")
}

buildkonfig {
    packageName = "com.well.sharedMobile"

    defaultConfigs {
        val dotEnv = DotEnv(project)
        mapOf(
            "google_web_client_id" to dotEnv.googleWebClientIdFull,
            "apple_server_client_id" to dotEnv["APPLE_SEVER_CLIENT_ID"],
            "apple_auth_redirect_url" to dotEnv["APPLE_AUTH_REDIRECT_URL"],
        ).forEach {
            buildConfigField(STRING, it.key, it.value)
        }
    }
}

kotlin {
    androidWithAndroid()
    iosWithSimulator()
    sourceSets {
        usePredefinedExperimentalAnnotations()
        val commonMain by getting {
            libDependencies(
                ":modules:atomic",
                ":modules:models",
                ":modules:utils",
                ":modules:viewHelpers",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "ktor.client.core",
                "shared.napier",
            )
        }
        if (withAndroid) {
            val androidMain by getting {
                libDependencies(
                    "android.facebookLogin",
                    "google.playServicesAuth",
                    "kotlin.coroutines.playServices",
                )
            }
        }
        val iosMain by getting {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}