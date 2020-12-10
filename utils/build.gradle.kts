plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    android()
    ios()
    sourceSets {
        val commonMain by getting {
            libDependencies(
                "kotlin.coroutines.core",
                "kotlin.stdLib"
            )
        }
        val commonTest by getting {
            libDependencies(
                "tests.kotest.assertionsCore",
                "tests.kotest.common"
//                "tests.kotest.property",
//                "tests.kotest.console"
            )

            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}