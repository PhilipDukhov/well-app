plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            libDependencies(
                ":modules:annotations",
            )
            dependencies {
                implementation("com.squareup:kotlinpoet:1.9.0")
            }
        }
    }
}

