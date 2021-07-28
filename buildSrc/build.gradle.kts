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

apply(from = "${projectDir.parent}/dependenciesResolver.gradle.kts")
apply(from = "${projectDir.parent}/dependencies.gradle")
val kotlinVersion = (extra["Versions"] as Map<*, *>)["kotlin"] as String
val gradlePluginVersion = extra.properties["gradlePluginVersion"] as? String
val dotEnv = ((extra["Libs"] as Map<*, *>)["build"] as Map<*, *>)["dotenv"] as String

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
    implementation(dotEnv)
}