plugins {
    `kotlin-dsl`
    groovy
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}
apply(from = "${projectDir.parent}/dependenciesResolver.gradle.kts")
apply(from = "${projectDir.parent}/dependencies.gradle")
val kotlinVersion = (extra["Versions"] as Map<*, *>)["kotlinBuildSrc"] as String
val gradlePluginVersion = extra.properties["gradlePluginVersion"] as String
val dotEnv = ((extra["Libs"] as Map<*, *>)["build"] as Map<*, *>)["dotenv"] as String

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("com.android.tools.build:gradle:$gradlePluginVersion")
    implementation(gradleApi())
    implementation(dotEnv)
}