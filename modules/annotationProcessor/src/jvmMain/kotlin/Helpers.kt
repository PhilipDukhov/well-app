package com.well.modules.annotationProcessor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

fun TypeSpec.Builder.dataClassConstructorParameters(
    vararg parameters: PropertyInfo,
    modifiers: List<KModifier> = emptyList(),
): TypeSpec.Builder {
    val builder = FunSpec.constructorBuilder()
    builder.addModifiers(modifiers)
    parameters.forEach { parameter ->
        builder.addParameter(
            ParameterSpec.builder(
                name = parameter.name,
                type = parameter.className
            ).defaultValue(parameter.defaultValue).build()
        )
        addProperty(
            PropertySpec.builder(
                name = parameter.name,
                type = parameter.className
            ).initializer(parameter.name)
                .addModifiers(parameter.modifiers)
                .build()
        )
    }
    primaryConstructor(builder.build())
    return this
}

class PropertyInfo(
    val name: String,
    val className: ClassName,
    vararg modifiers: KModifier,
    val defaultValue: CodeBlock? = null,
) {
    val modifiers: List<KModifier> = modifiers.toList()
}