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
    jvm()
    sourceSets {
        optIns()
        val commonMain by getting {
            libDependencies(
                "kotlin.coroutines.core",
                "kotlin.reflect",
            )
        }
        if (withAndroid) {
            val androidMain by getting {
                kotlin.srcDir("src/jvmMain/kotlin")
            }
        }
    }
}
