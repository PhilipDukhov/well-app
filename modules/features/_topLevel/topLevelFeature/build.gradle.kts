plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("kapt")
}

val generatedKotlinSources: String = "$projectDir/src/gen/kotlin"

kapt {
    javacOptions {
        option("-Akapt.kotlin.generated=$generatedKotlinSources")
    }
}

kotlin {
    android()
    iosWithSimulator(project = project)
    sourceSets {
        optIns(OptIn.Coroutines)
        val commonMain by getting {
            libDependencies(
                ":modules:annotations",
                ":modules:utils:flowUtils",
                ":modules:utils:kotlinUtils",
                ":modules:utils:viewUtils",
                ":modules:models",
                ":modules:puerhBase",

                ":modules:features:more:moreFeature",
                ":modules:features:welcome",
                ":modules:features:myProfile:myProfileFeature",
                ":modules:features:login:loginFeature",
                ":modules:features:call:callFeature",
                ":modules:features:chatList:chatListFeature",
                ":modules:features:experts:expertsFeature",
                ":modules:features:userChat:userChatFeature",
                ":modules:features:calendar:calendarFeature",
                ":modules:features:updateRequest",

                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "shared.napier",
                "kotlin.datetime",
            )

            // Workaround for lack of Kapt support in multiplatform project:
            dependencies.add("kapt", project(":modules:annotationProcessor"))
            kotlin.srcDir(generatedKotlinSources)
        }
        val iosMain by getting {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}
