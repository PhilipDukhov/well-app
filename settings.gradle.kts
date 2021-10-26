rootProject.name = "WELL"

apply(from = "dependenciesResolver.gradle.kts")
val withAndroid = System.getProperty("withAndroid")!!.toBoolean()

val platform = try {
    extra["kotlin.native.cocoapods.platform"] as? String
} catch (t: Throwable) {
    null
}
if (platform != "iphonesimulator") {
    if (withAndroid) {
        include("androidApp")
        include("androidAppTest")
        include(":modules:androidUi")
        include(":modules:androidWebrtc")
    }
    include(":sharedMobile")
}

include(
    ":server",
    ":sharedMobileTest",
    ":modules:annotations",
    ":modules:annotationProcessor",
    ":modules:models",
    ":modules:atomic",
    ":modules:utils:ktorUtils",
    ":modules:utils:kotlinUtils",
    ":modules:utils:viewUtils",
    ":modules:utils:flowUtils",
    ":modules:db:usersDb",
    ":modules:db:chatMessagesDb",
    ":modules:db:serverDb",
    ":modules:db:mobileDb",
    ":modules:db:helperDb",
    ":modules:utils:flowUtils",
    ":modules:networking",
    ":modules:puerhBase",

    ":modules:features:welcome",
    ":modules:features:more",
    ":modules:features:myProfile",
    ":modules:features:login:loginFeature",
    ":modules:features:login:loginHandlers",
    ":modules:features:call:callFeature",
    ":modules:features:call:callHandlers",
    ":modules:features:chatList:chatListFeature",
    ":modules:features:chatList:chatListHandlers",
    ":modules:features:experts:expertsFeature",
    ":modules:features:experts:expertsHandlers",
    ":modules:features:userChat:userChatFeature",
    ":modules:features:userChat:userChatHandlers",
)

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