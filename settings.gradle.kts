rootProject.name = "WELL"

enum class Executor {
    AndroidStudio,
    Idea,
    Cocoapods,
    CocoapodsArm64Simulator,
    Console,
}

apply(from = "dependenciesResolver.gradle.kts")
val withAndroid = System.getProperty("withAndroid")!!.toBoolean()
val executor = Executor.values()[System.getProperty("executor")!!.toInt()]

val includeAndroid = listOf(
    Executor.AndroidStudio,
    Executor.Console,
).contains(executor)
val includeServer = listOf(
    Executor.AndroidStudio,
    Executor.Idea,
    Executor.Console,
).contains(executor)
val includeSharedMobile = executor != Executor.Idea
if (includeAndroid) {
    include(
        "androidApp",
        "androidAppTest",
        ":modules:androidUi",
        ":modules:androidWebrtc",
    )
}
if (includeServer) {
    include(
        ":server",
        ":modules:db:serverDb",
    )
}
if (includeSharedMobile) {
    if (executor != Executor.CocoapodsArm64Simulator) {
        include(":sharedMobile")
    }
    include(
        ":sharedMobileTest",
        ":modules:atomic",
        ":modules:utils:viewUtils",
        ":modules:db:mobileDb",
        ":modules:networking",
        ":modules:puerhBase",
        ":modules:features:welcome",
        ":modules:features:more",
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
    )
}

include(
    ":modules:annotations",
    ":modules:annotationProcessor",
    ":modules:models",
    ":modules:utils:ktorUtils",
    ":modules:utils:kotlinUtils",
    ":modules:utils:flowUtils",
    ":modules:db:usersDb",
    ":modules:db:chatMessagesDb",
    ":modules:db:helperDb",
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