plugins {
    kotlin("multiplatform")
    if (withAndroid) {
        id("com.android.library")
    }
    kotlin("kapt")
}

val generatedKotlinSources: String = "$projectDir/src/gen/kotlin"

kapt {
    javacOptions {
        option("-Akapt.kotlin.generated=$generatedKotlinSources")
    }
}

kotlin {
    androidWithAndroid()
    iosWithSimulator(project = project)
    sourceSets {
        optIns(optIns = setOf(OptIn.Coroutines))
        val commonMain by getting {
            libDependencies(
                ":modules:annotations",
                ":modules:utils:flowUtils",
                ":modules:utils:kotlinUtils",
                ":modules:utils:viewUtils",
                ":modules:models",
                ":modules:puerhBase",

                ":modules:features:more",
                ":modules:features:welcome",
                ":modules:features:myProfile:myProfileFeature",
                ":modules:features:login:loginFeature",
                ":modules:features:call:callFeature",
                ":modules:features:chatList:chatListFeature",
                ":modules:features:experts:expertsFeature",
                ":modules:features:userChat:userChatFeature",

                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "shared.napier",
                "kotlin.datetime",
            )

            // Workaround for lack of Kapt support in multiplatform project:
            if (withAndroid) {
                dependencies.add("kapt", project(":modules:annotationProcessor"))
            }
            kotlin.srcDir(generatedKotlinSources)
        }
        val iosMain by getting {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}
