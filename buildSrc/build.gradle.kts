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

apply(from = "../dependenciesResolver.gradle.kts")
val kotlinVersion = extra["kotlinVersion"]!! as String
val gradlePluginVersion = extra.properties["gradlePluginVersion"] as? String

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
    if (gradlePluginVersion != null) {
        implementation("com.android.tools.build:gradle:$gradlePluginVersion")
    }
    implementation(gradleApi())
}