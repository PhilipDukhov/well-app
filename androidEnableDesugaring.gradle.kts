import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin

plugins.matching { it is AppPlugin || it is LibraryPlugin }.whenPluginAdded {
    configure<BaseExtension> {
        compileOptions {
            isCoreLibraryDesugaringEnabled = true
        }
    }
}

dependencies {
    coreLibraryDesugaring()
}

