import org.jetbrains.kotlin.gradle.dsl.KotlinTargetContainerWithPresetFunctions

val withAndroid: Boolean
    get() = System.getProperty("withAndroid")!!.toBoolean()

fun KotlinTargetContainerWithPresetFunctions.androidWithAndroid() {
    if (withAndroid) {
        android()
    }
}