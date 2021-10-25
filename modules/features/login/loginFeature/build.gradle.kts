import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    kotlin("multiplatform")
    if (withAndroid) {
        id("com.android.library")
    }
}

kotlin {
    androidWithAndroid()
    iosWithSimulator(includeSimulator = true)
    sourceSets {
        val commonMain by getting {
            libDependencies(
                ":modules:puerhBase",
            )
        }
    }
}