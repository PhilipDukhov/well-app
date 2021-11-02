import org.jetbrains.kotlin.gradle.dsl.KotlinTargetContainerWithPresetFunctions

val withAndroid: Boolean
    get() = System.getProperty("withAndroid")!!.toBoolean()

fun KotlinTargetContainerWithPresetFunctions.androidWithAndroid() {
    if (withAndroid) {
        android()
    }
}

enum class Executor {
    AndroidStudio,
    Idea,
    Cocoapods,
    CocoapodsArm64Simulator,
    Console,
    // update other locations after adding/updating values
}

val executor: Executor
    get() = Executor.values()[System.getProperty("executor")!!.toInt()]