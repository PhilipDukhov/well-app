import org.gradle.api.Project
import java.io.File
import java.util.*

fun Project.localProperties(): Properties {
    val file = try {
        (File(projectDir.parent + "/local.properties")).inputStream()
    } catch (_: Exception) {
        project.rootProject.file("local.properties").inputStream()
    }
    val properties = Properties()
    properties.load(file)
    return properties
}