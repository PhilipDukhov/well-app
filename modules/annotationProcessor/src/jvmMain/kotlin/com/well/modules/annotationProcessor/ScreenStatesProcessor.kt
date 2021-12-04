@file:Suppress("MemberVisibilityCanBePrivate", "HasPlatformType")

package com.well.modules.annotationProcessor

import com.well.modules.annotations.ScreenStates
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic.Kind

@SupportedSourceVersion(SourceVersion.RELEASE_11)
class ScreenStatesProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes() = mutableSetOf(
        ScreenStates::class.java.canonicalName,
    )

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?,
    ): Boolean {
        roundEnv?.getElementsAnnotatedWith(ScreenStates::class.java)?.forEach { element ->
            if (element.kind != ElementKind.CLASS) {
                processingEnv.println(
                    Kind.ERROR,
                    "Can only be applied to classes, element: $element\n"
                )
                return false
            }
            val generatedSourcesRoot = processingEnv.options[kaptKotlinGeneratedOption]
            if (generatedSourcesRoot == null) {
                processingEnv.println(
                    Kind.ERROR,
                    "Can't find the target directory for generated Kotlin files.\n"
                )
                return false
            }
            try {
                ContainerInfo(element, processingEnv)
                    .file()
                    .writeTo(File(generatedSourcesRoot))
            } catch (t: Throwable) {
                processingEnv.println(
                    kind = Kind.ERROR,
                    msg = "ContainerInfo(element, processingEnv) $t\n${t.stackTraceToString()}"
                )
                throw t
            }
        }
        return true
    }
}

fun ProcessingEnvironment.println(
    kind: Kind,
    msg: String,
) = messager.printMessage(kind, "$msg\r\n")

class ContainerInfo(
    element: Element,
    val processingEnv: ProcessingEnvironment,
) {
    val pairClassName = ClassName("kotlin", "Pair")
    val setClassName = ClassName("kotlin.collections", "Set")
    val annotation = element.getAnnotation(ScreenStates::class.java)!!
    val packageName = processingEnv.elementUtils.getPackageOf(element).toString()
    val containerFeatureClassName = ClassName(packageName, element.simpleName.toString())
    val screenStateName = "ScreenState"
    val screenStateClassName = ClassName(packageName, screenStateName)
    val featureMsgContainerName = "FeatureMsg"
    val featureMsgContainerClassName = ClassName(packageName, featureMsgContainerName)
    val featureEffContainerName = "FeatureEff"
    val featureEffContainerClassName = ClassName(packageName, featureEffContainerName)
    val containerFeatureStateClassName = containerFeatureClassName.nestedClass("State")
    val featureTabType = processingEnv.elementUtils.getTypeElement(
        containerFeatureStateClassName.nestedClass("Tab").canonicalName
    )!!
    val containerFeatureScreenPositionClassName =
        containerFeatureStateClassName.nestedClass("ScreenPosition")
    val containerFeatureMsgClassName = containerFeatureClassName.nestedClass("Msg")
    val containerFeatureEffClassName = containerFeatureClassName.nestedClass("Eff")
    val reducerResultClassName = pairClassName.parameterizedBy(
        containerFeatureStateClassName,
        setClassName.parameterizedBy(containerFeatureEffClassName)
    )
    val featureInfos = annotation.safeFeatures.map {
        FeatureInfo(it, this, processingEnv)
    }
    val napierClassName = ClassName("io.github.aakira.napier", "Napier")
    val withEmptySetClassName = ClassName("com.well.modules.puerhBase", "withEmptySet")
    val letNamedClassName = ClassName("com.well.modules.utils.kotlinUtils", "letNamed")
    val pairMapSecondClassName = ClassName("com.well.modules.utils.kotlinUtils", "mapSecond")
    val setRemovingClassName = ClassName("com.well.modules.utils.kotlinUtils", "removing")

    val ScreenStates.safeFeatures: List<TypeMirror>
        get() {
            return try {
                features // should throw
                listOf()
            } catch (e: MirroredTypesException) {
                e.typeMirrors
            }
        }

    fun file() =
        FileSpec.builder(packageName, "ScreenState")
            .addType(featureMsgInterface())
            .addType(featureEffInterface())
            .addType(statesClass())
            .apply {
                val reduceScreenMsgFuncBuilder =
                    FunSpec
                        .builder("reduceScreenMsg")
                        .addParameter(
                            ParameterSpec.builder(
                                "msg",
                                featureMsgContainerClassName
                            ).build()
                        )
                        .addParameter(
                            ParameterSpec.builder(
                                "state",
                                containerFeatureStateClassName
                            ).build()
                        )
                        .returns(reducerResultClassName)
                        .addModifiers(KModifier.INTERNAL)
                        .beginControlFlow("return when(msg)")
                val reduceBackMsgFuncBuilder =
                    FunSpec
                        .builder("reduceBackMsg")
                        .addParameter(
                            ParameterSpec.builder(
                                "state",
                                containerFeatureStateClassName
                            ).build()
                        )
                        .returns(reducerResultClassName.copy(nullable = true))
                        .addModifiers(KModifier.INTERNAL)
                        .beginControlFlow("return when (val topScreen = state.topScreen)")
                featureInfos
                    .forEach { featureInfo ->
                        val reducerFunc = featureInfo.featureReducerFunc()
                        addFunction(reducerFunc)
                        reduceScreenMsgFuncBuilder.addStatement(
                            "is %T -> ${reducerFunc.name}(msg.position, msg.msg, state)",
                            featureInfo.featureMsgContainerClassName
                        )
                        if (featureInfo.featureBackMsg != null) {
                            reduceBackMsgFuncBuilder.addStatement(
                                "is %T.${featureInfo.featureShortName} -> ${reducerFunc.name}(topScreen.position, %T, state)",
                                screenStateClassName,
                                featureInfo.featureBackMsg,
                            )
                        }
                    }
                addFunction(
                    reduceScreenMsgFuncBuilder
                        .endControlFlow()
                        .build()
                )
                addFunction(
                    reduceBackMsgFuncBuilder
                        .addStatement("else -> null")
                        .endControlFlow()
                        .build()
                )
                addFunction(reduceScreenFunc())
            }
            .addAnnotation(
                AnnotationSpec.suppressWarningTypes("RedundantVisibilityModifier", "TrailingComma")
            )
            .build()

    fun reduceScreenFunc(): FunSpec {
        val screenState = TypeVariableName("SS", bounds = listOf(screenStateClassName))
            .copy(reified = true)
        val msg = TypeVariableName("M")
        val state = TypeVariableName("S")
        val eff = TypeVariableName("E")
        return FunSpec
            .builder("reduceScreen")
            .addModifiers(KModifier.INLINE, KModifier.INTERNAL)
            .apply {
                listOf(
                    screenState,
                    msg,
                    state,
                    eff,
                ).forEach(::addTypeVariable)
            }
            .addParameter(
                ParameterSpec.builder(
                    "position",
                    containerFeatureScreenPositionClassName
                ).build()
            )
            .addParameter(
                ParameterSpec.builder(
                    "msg",
                    msg
                ).build()
            )
            .addParameter(
                ParameterSpec.builder(
                    "state",
                    containerFeatureStateClassName
                ).build()
            )
            .addParameter(
                ParameterSpec.builder(
                    "reducer",
                    LambdaTypeName.get(
                        returnType = pairClassName.parameterizedBy(
                            state,
                            setClassName
                                .parameterizedBy(eff)
                        ),
                        parameters = arrayOf(msg, state),
                    )
                ).build()
            )
            .addParameter(
                ParameterSpec.builder(
                    "effCreator",
                    LambdaTypeName.get(
                        returnType = containerFeatureEffClassName,
                        parameters = arrayOf(eff, containerFeatureScreenPositionClassName),
                    )
                ).build()
            )
            .returns(reducerResultClassName)
            .addStatement(
                """
                val screen = state.tabs[position.tab]?.getOrNull(position.index)
                    ?: run {
                        %T.e("reduceScreen screen not found at ${'$'}position  ${'$'}msg | ${'$'}state")
                        return state.%T()
                    }
                val (newScreenState, effs) = reducer(msg, screen.baseState as S)
                val newEffs = effs.mapTo(HashSet()) {
                    effCreator(it, position)
                }
                return state.changeScreen<SS>(position) {
                    baseCopy(newScreenState as Any) as SS
                } to newEffs
                """,
                napierClassName,
                withEmptySetClassName,
            )
            .addAnnotation(
                AnnotationSpec.suppressWarningTypes("UNCHECKED_CAST")
            )
            .build()
    }

    fun featureEffInterface() =
        TypeSpec
            .interfaceBuilder(featureEffContainerName)
            .addModifiers(KModifier.SEALED)
            .addSuperinterface(containerFeatureEffClassName)
            .apply {
                val sealedSubclasses = featureInfos.map { it.featureEffContainer() }
                sealedSubclasses.forEach(::addType)
            }
            .build()

    fun featureMsgInterface() =
        TypeSpec
            .classBuilder(featureMsgContainerName)
            .addModifiers(KModifier.SEALED)
            .superclass(containerFeatureMsgClassName)
            .apply {
                val sealedSubclasses = featureInfos.map { it.featureMsgContainer() }
                sealedSubclasses.forEach(::addType)
            }
            .build()

    fun statesClass() =
        TypeSpec
            .classBuilder(screenStateName)
            .addModifiers(KModifier.SEALED)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        ParameterSpec.builder(
                            "baseState",
                            Any::class
                        ).addModifiers(KModifier.INTERNAL).build()
                    ).build()
            )
            .addProperty(
                PropertySpec.builder("baseState", Any::class)
                    .addModifiers(KModifier.INTERNAL)
                    .initializer("baseState")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("position", containerFeatureScreenPositionClassName)
                    .addModifiers(KModifier.ABSTRACT)
                    .build()
            )
            .addFunction(
                FunSpec.baseCopy()
                    .addModifiers(KModifier.INTERNAL, KModifier.ABSTRACT)
                    .returns(screenStateClassName)
                    .build()
            )
            .apply {
                val sealedSubclasses = featureInfos.map { it.featureClass() } +
                        annotation.empties.map { emptyStateType(it, screenStateClassName) }
                sealedSubclasses.forEach(::addType)
            }
            .build()

    fun emptyStateType(
        name: String,
        screenStateClassName: ClassName,
    ) = TypeSpec
        .objectBuilder(name)
        .superclass(screenStateClassName)
        .addSuperclassConstructorParameter("Unit")
        .addProperty(
            PropertySpec.builder(
                name = "position",
                type = containerFeatureScreenPositionClassName
            )
                .addModifiers(KModifier.OVERRIDE)
                .getter(
                    FunSpec.getterBuilder()
                        .addStatement(
                            "return throw IllegalStateException()",
                        )
                        .build()
                )
                .build()
        )
        .addFunction(
            FunSpec.baseCopy()
                .addModifiers(KModifier.OVERRIDE)
                .addStatement(
                    "return throw IllegalStateException()",
                )
                .returns(screenStateClassName)
                .build()
        )
        .build()
}

class FeatureInfo(
    featureTypeMirror: TypeMirror,
    val containerInfo: ContainerInfo,
    processingEnv: ProcessingEnvironment,
) {
    val feature = processingEnv.typeUtils.asElement(featureTypeMirror)!! as TypeElement
    val featureBackMsg = processingEnv.elementUtils.getTypeElement("$feature.Msg.Back")
    val featurePushEff = processingEnv.elementUtils.getTypeElement("$feature.Eff.Push")
    val featureName = feature.simpleName.toString()

    init {
        if (!featureName.endsWith("Feature")) {
            throw IllegalStateException("$featureName; Only features can be applied for screen state")
        }
    }

    val featureShortName = featureName.removeSuffix("Feature")
    val featureStateTab = containerInfo.featureTabType.enclosedElements
        .find { it.simpleName.toString() == featureShortName }
    val featurePackage = processingEnv.elementUtils.getPackageOf(feature).qualifiedName.toString()
    val featureClassName = ClassName(
        featurePackage,
        featureName
    )
    val featureStateClassName = featureClassName.nestedClass("State")
    val featureMsgClassName = featureClassName.nestedClass("Msg")
    val featureMsgContainerClassName =
        containerInfo.featureMsgContainerClassName.nestedClass(featureShortName)
    val featureEffContainerClassName =
        containerInfo.featureEffContainerClassName.nestedClass(featureShortName)
    val featureEffClassName = featureClassName.nestedClass("Eff")
    val screenClassName = containerInfo.screenStateClassName.nestedClass(featureShortName)
    val featureScreenState =
        processingEnv.elementUtils.getTypeElement("$featurePackage.${featureShortName}ScreenState")

    fun featureMsgContainer() =
        TypeSpec
            .classBuilder(featureShortName)
            .superclass(containerInfo.featureMsgContainerClassName)
            .dataClassConstructorParameters(
                PropertyInfo(
                    name = "msg",
                    className = featureMsgClassName,
                ),
                PropertyInfo(
                    name = "position",
                    className = containerInfo.containerFeatureScreenPositionClassName,
                    defaultValue = featureStateTab?.let {
                        CodeBlock.of(
                            "%T(tab = %T.${it.simpleName}, index = 0)",
                            containerInfo.containerFeatureScreenPositionClassName,
                            it,
                        )
                    },
                ),
            )
            .build()

    fun featureEffContainer() =
        TypeSpec
            .classBuilder(featureShortName)
            .addSuperinterface(containerInfo.featureEffContainerClassName)
            .dataClassConstructorParameters(
                PropertyInfo(
                    name = "eff",
                    className = featureEffClassName,
                ),
                PropertyInfo(
                    name = "position",
                    className = containerInfo.containerFeatureScreenPositionClassName,
                ),
            )
            .build()

    fun featureClass() =
        TypeSpec
            .classBuilder(featureShortName)
            .addModifiers(KModifier.DATA)
            .dataClassConstructorParameters(
                PropertyInfo(
                    name = "position",
                    className = containerInfo.containerFeatureScreenPositionClassName,
                    KModifier.OVERRIDE,
                ),
                PropertyInfo(
                    name = "state",
                    className = featureStateClassName,
                ),
                modifiers = listOf(KModifier.INTERNAL),
            )
            .superclass(containerInfo.screenStateClassName)
            .addSuperclassConstructorParameter("state")
            .addFunction(
                FunSpec.baseCopy()
                    .addModifiers(KModifier.OVERRIDE)
                    .addStatement(
                        "return copy(state = state as %T)",
                        featureStateClassName
                    )
                    .returns(containerInfo.screenStateClassName)
                    .build()
            )
            .addFunction(
                FunSpec.builder("mapMsgToTopLevel")
                    .addParameter(name = "msg", type = featureMsgClassName)
                    .returns(containerInfo.containerFeatureMsgClassName)
                    .addStatement(
                        "return %T(msg = msg, position = position)",
                        featureMsgContainerClassName
                    )
                    .build()
            )
            .build()

    fun featureReducerFunc() =
        FunSpec
            .builder("reduce$featureShortName")
            .addParameter(
                ParameterSpec.builder(
                    "position",
                    containerInfo.containerFeatureScreenPositionClassName,
                ).build()
            )
            .addParameter(
                ParameterSpec.builder(
                    "msg",
                    featureMsgClassName
                ).build()
            )
            .addParameter(
                ParameterSpec.builder(
                    "state",
                    containerInfo.containerFeatureStateClassName
                ).build()
            )
            .returns(containerInfo.reducerResultClassName)
            .addStatement(
                """
                val (newState, effs) = reduceScreen<
                    %T,
                    %T,
                    %T,
                    %T
                    >(
                    position,
                    msg,
                    state,
                    %T::reducer,
                    %T::$featureShortName
                ) 
                """.trimIndent(),
                screenClassName,
                featureMsgClassName,
                featureStateClassName,
                featureEffClassName,
                featureClassName,
                containerInfo.featureEffContainerClassName
            )
            .apply {
                if (featurePushEff != null) {
                    addStatement(
                        """
                        effs.mapNotNull {
                            val pushEff = (it as? %1T)?.eff as? %2T ?:
                            return@mapNotNull null
                            it to pushEff.screen
                        }.firstOrNull()
                            ?.%3T { pushEff, screen ->
                                val finalState = when (screen) {
                        """.trimIndent(),
                        featureEffContainerClassName,
                        featurePushEff,
                        containerInfo.letNamedClassName,
                    )
                    featureScreenState.enclosedElements
                        .filter { it.kind != ElementKind.CONSTRUCTOR }
                        .forEach { screenState ->
                            addStatement(
                                """
                                is $screenState -> { 
                                    newState.copyPush(
                                        state = screen.state, 
                                        createScreen = %L
                                    )
                                }
                                """.trimIndent(),
                                containerInfo.screenStateClassName.nestedClass(screenState.simpleName.toString())
                                    .constructorReference()
                            )
                        }
                    addStatement(
                        """
                                }
                                return finalState
                                    .reduceScreenChanged()
                                    .%T { it.%T(pushEff) }
                            }
                        """.trimIndent(),
                        containerInfo.pairMapSecondClassName,
                        containerInfo.setRemovingClassName,
                    )
                }
            }
            .addStatement("return newState to effs")
            .addModifiers(KModifier.INTERNAL)
            .build()
}

fun FunSpec.Companion.baseCopy() =
    builder("baseCopy")
        .addParameter(
            ParameterSpec.builder(
                "state",
                Any::class
            )
                .build()
        )

fun AnnotationSpec.Companion.suppressWarningTypes(vararg types: String) =
    builder(ClassName("", "Suppress"))
        .addMember("%S,".repeat(types.count()).trimEnd(','), *types)
        .build()

const val kaptKotlinGeneratedOption = "kapt.kotlin.generated"
