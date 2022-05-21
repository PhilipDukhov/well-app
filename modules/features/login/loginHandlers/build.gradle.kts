import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    kotlin("multiplatform")
    id("com.android.library")
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
    android()
    iosWithSimulator(project = project)
    sourceSets {
        val commonMain by getting {
            libDependencies(
                ":modules:models",
                ":modules:networking",
                ":modules:utils:viewUtils",
                ":modules:features:login:loginFeature",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "ktor.client.core",
                "shared.napier",
            )
        }
        val androidMain by getting {
            libDependencies(
                "android.facebookLogin",
                "google.playServicesAuth",
                "kotlin.coroutines.playServices",
            )
        }
        findByName("iosMain")?.run {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}