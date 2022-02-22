plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
}

kotlin {
    android()
    val frameworkName = "SharedMobile"
    iosWithSimulator(project = project) {
        binaries {
            framework(frameworkName) {
                freeCompilerArgs += listOf("-Xobjc-generics")
            }
        }
    }
    cocoapods {
        framework {
            baseName = frameworkName
        }
        summary = frameworkName
        homepage = "-"
        ios.deploymentTarget = project.version("iosDeploymentTarget")
    }
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
        val iosMain by getting {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}
