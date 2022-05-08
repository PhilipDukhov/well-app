plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
}

kotlin {
    android()
    iosWithSimulator(project = project)
    jvm()
    sourceSets {
        optIns(OptIn.Serialization)
        val commonMain by getting {
            libDependencies(
                "kotlin.coroutines.core",
                "kotlin.serializationJson",
                "kotlin.reflect",
            )
        }
        val androidMain by getting {
            kotlin.srcDir("src/jvmMain/kotlin")
        }
    }
}
