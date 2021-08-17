package com.joyy.neza_compiler.processor.methodChannel

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
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.annotations.Nullable
import javax.annotation.processing.Filer
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

class SenderProcessor(
    private val elementUtils: Elements,
    private val typeUtils: Types,
    private val filer: Filer,
    private val printer: Printer
) {

    fun handle(
        roundEnv: RoundEnvironment,
        element: Element,
        channelReceiverMap: HashMap<String, ClassName>
    ) {
        val clazzName = element.simpleName.toString()
        val generateClazzName = "${clazzName}Impl"
        val channelAnnotation = element.getAnnotation(FlutterMethodChannel::class.java)
        val channelName = channelAnnotation.channelName
        val kind = element.kind

        if (kind != ElementKind.INTERFACE) {
            printer.error("The sender of Method channel must be a interface.[$clazzName]")
        }

        val enclosedElements = (element as TypeElement).enclosedElements
        val functions = ArrayList<FunSpec>()
        if (enclosedElements != null) {
            for (method in enclosedElements) {
                if (method !is ExecutableElement) {
                    continue
                }
                functions.addAll(
                    assembleFunction(clazzName, method, channelName, channelReceiverMap)
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
        clazzName: String,
        method: ExecutableElement,
        channelName: String,
        channelReceiverMap: HashMap<String, ClassName>
    ): ArrayList<FunSpec> {
        val list = ArrayList<FunSpec>()
        var receiverClassName = channelReceiverMap[channelName]
        if (receiverClassName == null) {
            receiverClassName = ClassName(
                ClazzConfig.PACKAGE.NEZA_CHANNEL,
                "${clazzName}Proxy"
            )
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

        val params = HashMap::class.asClassName().parameterizedBy(
            String::class.asTypeName(),
            Any::class.asClassName().copy(nullable = true)
        )

        val scopeClassName = ClassName(
            ClazzConfig.Coroutine.COROUTINE_X_PACKAGE,
            ClazzConfig.Coroutine.COROUTINE_SCOPE_NAME,
        )
        val dispatchersClassName = ClassName(
            ClazzConfig.Coroutine.COROUTINE_X_PACKAGE,
            ClazzConfig.Coroutine.COROUTINE_DISPATCHERS_NAME,
        )
        val launchClassName = ClassName(
            ClazzConfig.Coroutine.COROUTINE_X_PACKAGE,
            ClazzConfig.Coroutine.COROUTINE_LAUNCH_NAME,
        )
        val suspendCoroutineClassName = ClassName(
            ClazzConfig.Coroutine.COROUTINE_PACKAGE,
            ClazzConfig.Coroutine.COROUTINE_SUSPEND_COROUTINE_NAME,
        )
        val resumeClassName = ClassName(
            ClazzConfig.Coroutine.COROUTINE_PACKAGE,
            ClazzConfig.Coroutine.COROUTINE_RESUME_NAME,
        )

        val methodName = method.simpleName.toString()
        val syncFun = FunSpec.builder(methodName)
            .addModifiers(KModifier.OVERRIDE)
            .addParameters(parameterList)
            .addStatement("val params = %T()", params)
        if (parameters != null) {
            for (parameter in parameters) {
                val parameterName = parameter.simpleName.toString()
                syncFun.addStatement("params[%S] = $parameterName", parameterName)
            }
        }
        syncFun.beginControlFlow(
            "%T(%T.Main).%T",
            scopeClassName,
            dispatchersClassName,
            launchClassName
        ).addStatement("$methodName(params)")
            .endControlFlow()
        list.add(syncFun.build())

        val resultClassName = ClassName(
            ClazzConfig.METHOD_RESULT_MODEL_PACKAGE,
            ClazzConfig.METHOD_RESULT_NAME
        )
        val successClassName = ClassName(
            ClazzConfig.METHOD_RESULT_MODEL_PACKAGE,
            ClazzConfig.METHOD_RESULT_SUCCESS_NAME
        )
        val errorClassName = ClassName(
            ClazzConfig.METHOD_RESULT_MODEL_PACKAGE,
            ClazzConfig.METHOD_RESULT_ERROR_NAME
        )
        val typeClassName = ClassName(
            ClazzConfig.METHOD_RESULT_MODEL_PACKAGE,
            ClazzConfig.METHOD_RESULT_TYPE_NAME
        )
        val callbackClassName = ClassName(
            ClazzConfig.Channel.METHOD_RESULT_PACKAGE,
            ClazzConfig.Channel.METHOD_RESULT_NAME
        )

        val asyncFun = FunSpec.builder(methodName)
            .addModifiers(KModifier.SUSPEND)
            .addParameter("params", params)
            .returns(resultClassName)
            .beginControlFlow(
                "val result = %T<%T>",
                suspendCoroutineClassName,
                resultClassName
            )
            .beginControlFlow(
                "val callback = object : %T ",
                callbackClassName
            )

        // callback -> success
        asyncFun.beginControlFlow("override fun success(result: Any?)")
            .addStatement("it.%T(", resumeClassName)
            .addStatement("  %T(", resultClassName)
            .addStatement("    resultType = %T.SUCCESS,", typeClassName)
            .addStatement("    successResult = %T(result),", successClassName)
            .addStatement("  )")
            .addStatement(")")
            .endControlFlow()

        // callback -> error
        asyncFun.beginControlFlow("override fun error(errorCode: String?, errorMessage: String?, errorDetails: Any?) ")
            .addStatement("it.%T(", resumeClassName)
            .addStatement("  %T(", resultClassName)
            .addStatement("    resultType = %T.ERROR,", typeClassName)
            .addStatement("    errorResult = %T(", errorClassName)
            .addStatement("      errorCode = errorCode,")
            .addStatement("      errorMessage = errorMessage,")
            .addStatement("      errorDetails = errorDetails")
            .addStatement("    )")
            .addStatement("  )")
            .addStatement(")")
            .endControlFlow()

        // callback -> notImplemented
        asyncFun.beginControlFlow("override fun notImplemented() ")
            .addStatement("it.%T(", resumeClassName)
            .addStatement("  %T(", resultClassName)
            .addStatement("    resultType = %T.NOT_IMPLEMENTED,", typeClassName)
            .addStatement("  )")
            .addStatement(")")
            .endControlFlow()

        asyncFun.endControlFlow()
            .addStatement("%T.instance", receiverClassName)
            .addStatement("  .getChannel()")
            .addStatement("  ?.invokeMethod(%S, params, callback)", methodName)
            .endControlFlow()
            .addStatement("return result")

        list.add(asyncFun.build())

        return list
    }
}