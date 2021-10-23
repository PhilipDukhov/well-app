rootProject.name = "WELL"

apply(from = "dependenciesResolver.gradle.kts")
val withAndroid = System.getProperty("withAndroid")!!.toBoolean()

include(
    ":server",
    ":sharedMobile",
//    ":sharedMobileTest",
    ":modules:annotations",
    ":modules:annotationProcessor",
    ":modules:models",
    ":modules:atomic",
    ":modules:utils",
    ":modules:db:usersDb",
    ":modules:db:chatMessagesDb",
    ":modules:db:serverDb",
    ":modules:db:mobileDb",
    ":modules:db:helperDb",
    ":modules:flowHelper",
    ":modules:networking",
    ":modules:viewHelpers",
    ":modules:features:call",
    ":modules:features:login",
    ":modules:features:chatList",
    ":modules:features:experts",
    ":modules:features:more",
    ":modules:features:myProfile",
    ":modules:features:userChat",
    ":modules:features:welcome",
)
if (withAndroid) {
    include("androidApp")
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            val pluginId = requested.id.id
            when {
                pluginId == "com.android" -> {
                    useModule("com.android.tools.build:gradle:${System.getProperty("gradlePluginVersion")}")
                }
            }
        }
    }
}