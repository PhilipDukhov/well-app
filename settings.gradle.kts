rootProject.name = "WELL"

apply(from = "dependenciesResolver.gradle.kts")
include(
    ":androidApp",
    ":androidAppTest",
    ":modules:androidUi",
    ":modules:androidWebrtc",
)
include(
    ":server",
    ":modules:db:serverDb",
)
include(
    ":sharedMobileTest",
    ":modules:atomic",
    ":modules:utils:viewUtils",
    ":modules:db:mobileDb",
    ":modules:networking",
    ":modules:puerhBase",
    ":modules:features:welcome",
    ":modules:features:more:moreFeature",
    ":modules:features:_topLevel:topLevelFeature",
    ":modules:features:_topLevel:topLevelHandlers",
    ":modules:features:myProfile:myProfileFeature",
    ":modules:features:myProfile:myProfileHandlers",
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
    ":modules:features:calendar:calendarFeature",
    ":modules:features:calendar:calendarHandlers",
    ":modules:features:notifications",
    ":modules:features:updateRequest",
)

include(
    ":modules:annotations",
    ":modules:annotationProcessor",
    ":modules:models",
    ":modules:utils:ktorUtils",
    ":modules:utils:kotlinUtils",
    ":modules:utils:flowUtils",
    ":modules:db:usersDb",
    ":modules:db:chatMessagesDb",
    ":modules:db:meetingsDb",
    ":modules:utils:dbUtils",
    ":modules:utils:flowUtils",
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