@file:Suppress("MemberVisibilityCanBePrivate", "HasPlatformType")

package com.well.modules.annotationProcessor

import com.well.modules.annotations.ScreenStates
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
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
        roundEnv: RoundEnvironment?
    ): Boolean {
        roundEnv?.getElementsAnnotatedWith(ScreenStates::class.java)?.forEach { element ->
            println("getElementsAnnotatedWith $element")
            if (element.kind != ElementKind.CLASS) {
                processingEnv.println(
                    Kind.ERROR,
                    "Can only be applied to functions, element: $element\n"
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
                processingEnv.println(Kind.ERROR, "ContainerInfo(element, processingEnv) $t\n${t.stackTraceToString()}")
                throw t
            }
        }
        return true
    }
}

fun ProcessingEnvironment.println(
    kind: Kind,
    msg: String
) = messager.printMessage(kind, "$msg\r\n")

class ContainerInfo(
    element: Element,
    val processingEnv: ProcessingEnvironment,
) {
    val pairClassName = ClassName("kotlin", "Pair")
    val setClassName = ClassName("kotlin.collections", "Set")
    val annotation = element.getAnnotation(ScreenStates::class.java)!!
    val packageName = processingEnv.elementUtils.getPackageOf(element).toString()
    val screenStateName = "ScreenState"
    val containerFeatureClassName = ClassName(packageName, element.simpleName.toString())
    val screenStateClassName = ClassName(packageName, screenStateName)
    val containerFeatureStateClassName = containerFeatureClassName.nestedClass("State")
    val containerFeatureMsgClassName = containerFeatureClassName.nestedClass("Msg")
    val containerFeatureEffClassName = containerFeatureClassName.nestedClass("Eff")
    val reducerResultClassName = pairClassName.parameterizedBy(
        containerFeatureStateClassName,
        setClassName.parameterizedBy(containerFeatureEffClassName)
    )
    val featureInfos = annotation.safeFeatures.map {
        FeatureInfo(it, this, processingEnv)
    }
    val napierClassName = ClassName("com.well.modules.napier", "Napier")
    val withEmptySetClassName = ClassName("com.well.modules.utils", "withEmptySet")
    val letNamedClassName = ClassName("com.well.modules.utils", "letNamed")

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
            .addType(statesClass())
            .apply {
                val reduceScreenMsgFuncBuilder =
                    FunSpec
                        .builder("reduceScreenMsg")
                        .addParameter(
                            ParameterSpec.builder(
                                "msg",
                                containerFeatureMsgClassName
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
                        .beginControlFlow("return when (state.topScreen)")
                featureInfos
                    .forEach { featureInfo ->
                        val reducerFunc = featureInfo.featureReducerFunc()
                        addFunction(reducerFunc)
                        reduceScreenMsgFuncBuilder.addStatement(
                            "is %T.${featureInfo.featureShortName}Msg -> ${reducerFunc.name}(msg.msg, state)",
                            containerFeatureMsgClassName
                        )
                        if (featureInfo.featureBackMsg != null) {
                            reduceBackMsgFuncBuilder.addStatement(
                                "is %T.${featureInfo.featureShortName} -> ${reducerFunc.name}(%T, state)",
                                screenStateClassName,
                                featureInfo.featureBackMsg,
                            )
                        }
                    }
                addFunction(
                    reduceScreenMsgFuncBuilder
                        .addStatement("else -> throw IllegalStateException()")
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
                AnnotationSpec.suppressWarningTypes("RedundantVisibilityModifier")
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
                        parameters = arrayOf(eff),
                    )
                ).build()
            )
            .returns(reducerResultClassName)
            .addStatement(
                """
                val (screen, position) = state.tabs.screenAndPositionOfTopOrNull<SS>(state)
                    ?: run {
                        %T.e("reduceScreen ${'$'}msg | ${'$'}state")
                        return state.%T()
                    }
                val (newScreenState, effs) = reducer(msg, screen.baseState as S)
                val newEffs = effs.mapTo(HashSet(), effCreator)
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
        screenStateClassName: ClassName
    ) = TypeSpec
        .objectBuilder(name)
        .superclass(screenStateClassName)
        .addSuperclassConstructorParameter("Unit")
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
    val featurePackage = processingEnv.elementUtils.getPackageOf(feature).qualifiedName.toString()
    val featureClassName = ClassName(
        featurePackage,
        featureName
    )
    val featureStateClassName = featureClassName.nestedClass("State")
    val featureMsgClassName = featureClassName.nestedClass("Msg")
    val featureEffClassName = featureClassName.nestedClass("Eff")
    val screenClassName = containerInfo.screenStateClassName.nestedClass(featureShortName)

    fun featureClass() =
        TypeSpec
            .classBuilder(featureShortName)
            .addModifiers(KModifier.DATA)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        ParameterSpec.builder(
                            "state",
                            featureStateClassName
                        ).build()
                    ).build()
            )
            .addProperty(
                PropertySpec.builder("state", featureStateClassName)
                    .initializer("state")
                    .build()
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
            .build()

    fun featureReducerFunc() =
        FunSpec
            .builder("reduce$featureShortName")
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
                    msg,
                    state,
                    %T::reducer,
                    %T::${featureShortName}Eff
                ) 
                """.trimIndent(),
                screenClassName,
                featureMsgClassName,
                featureStateClassName,
                featureEffClassName,
                featureClassName,
                containerInfo.containerFeatureEffClassName
            )
            .apply {
                if (featurePushEff != null) {
                    addStatement(
                        """
                        effs.mapNotNull {
                            val pushEff = (it as? %1T.${featureShortName}Eff)?.eff as? %2T ?:
                            return@mapNotNull null
                            it to pushEff.screen
                        }.firstOrNull()
                            ?.%3T { pushEff, screen ->
                                return newState.copyPush(screen = screen) to
                                        effs.toMutableSet().apply {
                                            remove(pushEff)
                                            add(%1T.TopScreenUpdated(screen))
                                        }
                            }
                        """.trimIndent(),
                        containerInfo.containerFeatureEffClassName,
                        featurePushEff,
                        containerInfo.letNamedClassName,
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
