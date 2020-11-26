plugins {
    `kotlin-dsl`
    groovy
}

buildscript {
    repositories {
        gradlePluginPortal()
        jcenter()
        google()
        mavenCentral()
    }
}

repositories {
    gradlePluginPortal()
    jcenter()
    google()
    mavenCentral()
}

apply(from = "${projectDir.parent}/dependencies.gradle")
val kotlinVersion = (extra["Versions"] as Map<*, *>)["kotlin"] as String
val gradlePluginVersion = extra["gradlePluginVersion"] as String

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("com.android.tools.build:gradle:$gradlePluginVersion")
    implementation(gradleApi())
}