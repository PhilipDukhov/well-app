plugins {
    `kotlin-dsl`
    groovy
}

buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

apply(from = "${projectDir.parent}/dependencies.gradle")
val kotlinVersion = (extra["Versions"] as Map<*, *>)["kotlin"] as String
val gradlePluginVersion = extra["gradlePluginVersion"] as String

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib") {
        version {
            strictly(kotlinVersion)
        }
    }
    implementation("org.jetbrains.kotlin:kotlin-reflect") {
        version {
            strictly(kotlinVersion)
        }
    }
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("com.android.tools.build:gradle:$gradlePluginVersion")
    implementation(gradleApi())
}