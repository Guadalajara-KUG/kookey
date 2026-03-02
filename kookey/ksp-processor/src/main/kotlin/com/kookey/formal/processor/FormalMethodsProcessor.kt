package com.kookey.formal.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

class FormalMethodsProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val requiresSymbols = resolver.getSymbolsWithAnnotation("com.kookey.formal.Requires")
        val ensuresSymbols = resolver.getSymbolsWithAnnotation("com.kookey.formal.Ensures")
        
        val functions = (requiresSymbols + ensuresSymbols)
            .filterIsInstance<KSFunctionDeclaration>()
            .distinctBy { it.qualifiedName?.asString() }

        for (function in functions) {
            generateWrapper(function)
        }

        return emptyList()
    }

    private fun generateWrapper(function: KSFunctionDeclaration) {
        val packageName = function.packageName.asString()
        val originalName = function.simpleName.asString()
        val wrapperName = "${originalName}WithContracts"

        val requiresAnnotation = function.annotations.find { 
            it.shortName.asString() == "Requires" && it.annotationType.resolve().declaration.packageName.asString() == "com.kookey.formal"
        }
        val ensuresAnnotation = function.annotations.find { 
            it.shortName.asString() == "Ensures" && it.annotationType.resolve().declaration.packageName.asString() == "com.kookey.formal"
        }

        @Suppress("UNCHECKED_CAST")
        val requiresConditions = requiresAnnotation?.arguments?.firstOrNull()?.value as? List<String> ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val ensuresConditions = ensuresAnnotation?.arguments?.firstOrNull()?.value as? List<String> ?: emptyList()

        val funBuilder = FunSpec.builder(wrapperName)
            .addModifiers(function.modifiers.mapNotNull { it.toKModifier() })
            .returns(function.returnType!!.toTypeName())

        // Add parameters
        function.parameters.forEach { param ->
            funBuilder.addParameter(param.name!!.asString(), param.type.toTypeName())
        }

        // Add Pre-conditions (require)
        if (requiresConditions.isNotEmpty()) {
            requiresConditions.forEach { condition ->
                funBuilder.addStatement("require(%L) { %S }", condition, "Pre-condition failed: $condition")
            }
        }

        // Add Original Function Call
        val args = function.parameters.joinToString(", ") { it.name!!.asString() }
        val hasReturnType = function.returnType != null && function.returnType!!.resolve().declaration.qualifiedName?.asString() != "kotlin.Unit"
        
        if (hasReturnType) {
            funBuilder.addStatement("val result = %L(%L)", originalName, args)
        } else {
            funBuilder.addStatement("%L(%L)", originalName, args)
        }

        // Add Post-conditions (check)
        if (ensuresConditions.isNotEmpty()) {
            ensuresConditions.forEach { condition ->
                funBuilder.addStatement("check(%L) { %S }", condition, "Post-condition failed: $condition")
            }
        }

        // Return if necessary
        if (hasReturnType) {
            funBuilder.addStatement("return result")
        }

        val fileSpec = FileSpec.builder(packageName, "${originalName}ContractsGenerated")
            .addFunction(funBuilder.build())
            .build()

        fileSpec.writeTo(environment.codeGenerator, aggregating = false)
    }
}
