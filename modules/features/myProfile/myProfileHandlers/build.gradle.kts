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
                "kotlin.coroutines.core",
                "shared.napier",
            )
        }
    }
}