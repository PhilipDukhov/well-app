plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    android()
    val frameworkName = "SharedMobile"
    iosWithSimulator(
        project = project,
        config = {
            binaries {
                framework(frameworkName) {
                    freeCompilerArgs += listOf("-Xobjc-generics")
                }
            }
        },
        cocoapodsFrameworkName = frameworkName,
    )
    exportIosModules(project)
    sourceSets {
        optIns()
        val commonMain by getting {
            libDependencies(
                ":modules:atomic",
                ":modules:models",
                ":modules:features:myProfile:myProfileHandlers",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "shared.napier",
                "kotlin.datetime",
            )
        }
        findByName("iosMain")?.run {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}
