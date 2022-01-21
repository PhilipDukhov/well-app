import io.github.cdimascio.dotenv.dotenv
import org.gradle.api.Project

class DotEnv(project: Project) {
    private val dotenv = dotenv {
        directory = "${project.rootDir}/iosApp/Well/Supporting files/"
        filename = "Shared.xcconfig"
    }
    val facebookAppId = dotenv["SHARED_FACEBOOK_APP_ID"]
    val googleWebClientId = dotenv["ANDROID_GOOGLE_CLIENT_ID"]
    val googleWebClientIdFull = "$googleWebClientId.apps.googleusercontent.com"
    val gcmSenderId = dotenv["GCM_SENDER_ID"]
    val googleApiKey = dotenv["GOOGLE_API_KEY"]

    operator fun get(key: String) = dotenv[key]
}