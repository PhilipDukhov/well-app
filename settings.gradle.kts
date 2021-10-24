rootProject.name = "WELL"

apply(from = "dependenciesResolver.gradle.kts")
val withAndroid = System.getProperty("withAndroid")!!.toBoolean()

include(
    ":server",
    ":sharedMobile",
    ":sharedMobileTest",
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

    ":modules:features:welcome",
    ":modules:features:more",
    ":modules:features:login",
    ":modules:features:myProfile",
    ":modules:features:call:callFeature",
    ":modules:features:call:callHandlers",
    ":modules:features:chatList:chatListFeature",
    ":modules:features:chatList:chatListHandlers",
    ":modules:features:experts:expertsFeature",
    ":modules:features:experts:expertsHandlers",
    ":modules:features:userChat:userChatFeature",
    ":modules:features:userChat:userChatHandlers",
)
if (withAndroid) {
    include("androidApp")
    include("androidAppTest")
    include(":modules:androidUi")
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
include(":modules:androidWebrtc")
