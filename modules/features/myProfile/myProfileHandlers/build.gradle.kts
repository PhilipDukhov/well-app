plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    if (withAndroid) {
        id("com.android.library")
    }
}

kotlin {
    androidWithAndroid()
    iosWithSimulator(project = project)
    sourceSets {
        optIns()
        val commonMain by getting {
            libDependencies(
                ":modules:features:myProfile:myProfileFeature",
                ":modules:models",
                ":modules:utils:viewUtils",
                ":modules:utils:kotlinUtils",
                ":modules:utils:flowUtils",
                ":modules:utils:kotlinUtils",
                "kotlin.coroutines.core",
                "shared.napier",
            )
        }
    }
}