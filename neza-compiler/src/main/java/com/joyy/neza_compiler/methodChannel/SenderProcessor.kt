package com.joyy.neza_compiler.methodChannel

import com.joyy.neza_annotation.method.FlutterMethodChannel
import com.joyy.neza_compiler.Printer
import com.joyy.neza_compiler.config.ClazzConfig
import com.joyy.neza_compiler.utils.TypeChangeUtils
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.annotations.Nullable
import javax.annotation.processing.Filer
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass

class SenderProcessor(
    private val filer: Filer,
    private val printer: Printer
) {

    fun handle(
        roundEnv: RoundEnvironment,
        element: Element,
        channelReceiverMap: HashMap<String, ClassName>
    ) {
        val clazzName = element.simpleName
        val generateClazzName = "${clazzName}Impl"
        val channelAnnotation = element.getAnnotation(FlutterMethodChannel::class.java)
        val channelName = channelAnnotation.channelName
        val kind = element.kind

        if (kind != ElementKind.INTERFACE) {
            error("The sender of Method channel must be a interface.[$clazzName]")
        }

        val enclosedElements = (element as TypeElement).enclosedElements
        val functions = ArrayList<FunSpec>()
        if (enclosedElements != null) {
            for (method in enclosedElements) {
                if (method !is ExecutableElement) {
                    continue
                }
                functions.addAll(
                    assembleFunction(method, channelName, channelReceiverMap)
                )
            }
        }

        val engineCreatorClazzBuilder = TypeSpec.objectBuilder(generateClazzName)
            .addSuperinterface(element.asType().asTypeName())

        functions.forEach {
            engineCreatorClazzBuilder.addFunction(it)
        }

        FileSpec.get(ClazzConfig.PACKAGE.NEZA_CHANNEL, engineCreatorClazzBuilder.build())
            .writeTo(filer)
    }

    private fun assembleFunction(
        method: ExecutableElement,
        channelName: String,
        channelReceiverMap: HashMap<String, ClassName>
    ): ArrayList<FunSpec> {
        val list = ArrayList<FunSpec>()
        val receiverClassName = channelReceiverMap[channelName]
        if (receiverClassName == null) {
            printer.error("[Sender] Receiver is null.")
            return list
        }

        val parameters = method.parameters
        val parameterList = ArrayList<ParameterSpec>()
        if (parameters != null) {
            for (parameter in parameters) {
                var type = parameter.asType().asTypeName()
                type = TypeChangeUtils.change(type)
                val nullableAnnotation = parameter.getAnnotation(Nullable::class.java)
                if (nullableAnnotation != null) {
                    type = type.copy(nullable = true)
                }
                parameterList.add(
                    ParameterSpec.builder(
                        parameter.simpleName.toString(),
                        type
                    ).build()
                )
            }
        }
        val methodName = method.simpleName.toString()
        list.add(
            FunSpec.builder(methodName)
                .addModifiers(KModifier.OVERRIDE)
                .addParameters(parameterList)
                .addStatement("%T.instance", receiverClassName)
                .addStatement("  .getChannel()")
                .addStatement("  ?.invokeMethod(%S, null)", methodName)
                .build()
        )

        val params = Map::class.asClassName().parameterizedBy(
            String::class.asTypeName(),
            Any::class.asClassName()
        )
        list.add(
            FunSpec.builder(methodName)
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("params", params)
                .addStatement("%T.instance", receiverClassName)
                .addStatement("  .getChannel()")
                .addStatement("  ?.invokeMethod(%S, null)", methodName)
                .build()
        )

        return list
    }
}