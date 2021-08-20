package com.joyy.neza_compiler.processor.methodChannel

import com.joyy.neza_annotation.method.FlutterMethodChannel
import com.joyy.neza_compiler.Printer
import com.joyy.neza_compiler.config.ClazzConfig
import com.joyy.neza_compiler.processor.common.ParamType
import com.joyy.neza_compiler.processor.common.ProcessorHelper
import com.joyy.neza_compiler.utils.TypeChangeUtils
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
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
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

class SenderProcessor(
    private val elementUtils: Elements,
    private val typeUtils: Types,
    private val filer: Filer,
    private val printer: Printer
) {

    private val resultClassName = ClassName(
        ClazzConfig.METHOD_RESULT_MODEL_PACKAGE,
        ClazzConfig.METHOD_RESULT_NAME
    )
    private val successClassName = ClassName(
        ClazzConfig.METHOD_RESULT_MODEL_PACKAGE,
        ClazzConfig.METHOD_RESULT_SUCCESS_NAME
    )
    private val errorClassName = ClassName(
        ClazzConfig.METHOD_RESULT_MODEL_PACKAGE,
        ClazzConfig.METHOD_RESULT_ERROR_NAME
    )
    private val typeClassName = ClassName(
        ClazzConfig.METHOD_RESULT_MODEL_PACKAGE,
        ClazzConfig.METHOD_RESULT_TYPE_NAME
    )
    private val callbackClassName = ClassName(
        ClazzConfig.Flutter.METHOD_RESULT_PACKAGE,
        ClazzConfig.Flutter.METHOD_RESULT_NAME
    )
    private val suspendCoroutineClassName = ClassName(
        ClazzConfig.Coroutine.COROUTINE_PACKAGE,
        ClazzConfig.Coroutine.COROUTINE_SUSPEND_COROUTINE_NAME,
    )
    private val resumeClassName = ClassName(
        ClazzConfig.Coroutine.COROUTINE_PACKAGE,
        ClazzConfig.Coroutine.COROUTINE_RESUME_NAME,
    )
    private val deferredClassName = ClassName(
        ClazzConfig.Coroutine.COROUTINE_X_PACKAGE,
        ClazzConfig.Coroutine.DEFERRED_NAME
    ).parameterizedBy(
        resultClassName
    )

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
                    assembleFunction(
                        clazzName,
                        method,
                        channelName,
                        channelReceiverMap
                    )
                )
            }
        }

        val engineCreatorClazzBuilder = TypeSpec.objectBuilder(generateClazzName)

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

        val methodName = method.simpleName.toString()
        val parameters = method.parameters

        val asyncMethodName = "${methodName}Async"
        val paramType = ProcessorHelper.checkParam(printer, parameters)

        var receiverClassName = channelReceiverMap[channelName]
        if (receiverClassName == null) {
            receiverClassName = ClassName(
                ClazzConfig.PACKAGE.NEZA_CHANNEL,
                "${clazzName}Proxy"
            )
        }

        val parameterList = ArrayList<ParameterSpec>()
        for (parameter in parameters) {
            val typeName = parameter.asType()
            var type = typeName.asTypeName()

            if (typeName is DeclaredType) {
                val resultType = TypeChangeUtils.change(
                    typeName.asElement().asType().asTypeName()
                )
                if (resultType is ClassName) {
                    val genericsType = ProcessorHelper.getGenericsType(printer, typeName)
                    type = if (genericsType.isNotEmpty()) {
                        resultType.parameterizedBy(
                            genericsType
                        )
                    } else {
                        resultType
                    }
                }
            } else {
                type = TypeChangeUtils.change(type)
            }

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

        val params = HashMap::class.asClassName().parameterizedBy(
            String::class.asTypeName(),
            Any::class.asClassName().copy(nullable = true)
        )

        val list = ArrayList<FunSpec>()
        val function = createMethod(
            receiverClassName = receiverClassName,
            orgMethodName = methodName,
            paramType = paramType,
            params = params,
            methodParameters = parameters,
            parameterList = parameterList
        )
        list.add(function.build())

        return list
    }

    private fun createCallback(function: FunSpec.Builder) {
        // callback -> success
        function.beginControlFlow("override fun success(result: Any?)")
            .addStatement("it.%T(", resumeClassName)
            .addStatement("  %T(", resultClassName)
            .addStatement("    resultType = %T.SUCCESS,", typeClassName)
            .addStatement("    successResult = %T(result),", successClassName)
            .addStatement("  )")
            .addStatement(")")
            .endControlFlow()

        // callback -> error
        function.beginControlFlow("override fun error(errorCode: String?, errorMessage: String?, errorDetails: Any?) ")
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
        function.beginControlFlow("override fun notImplemented() ")
            .addStatement("it.%T(", resumeClassName)
            .addStatement("  %T(", resultClassName)
            .addStatement("    resultType = %T.NOT_IMPLEMENTED,", typeClassName)
            .addStatement("  )")
            .addStatement(")")
            .endControlFlow()
    }

    private fun createMethod(
        receiverClassName: ClassName,
        orgMethodName: String,
        paramType: ParamType,
        params: ParameterizedTypeName,
        methodParameters: List<VariableElement>,
        parameterList: List<ParameterSpec>,
    ): FunSpec.Builder {
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
        val asyncClassName = ClassName(
            ClazzConfig.Coroutine.COROUTINE_X_PACKAGE,
            ClazzConfig.Coroutine.COROUTINE_ASYNC_NAME,
        )

        val function = FunSpec.builder(orgMethodName)
            .returns(deferredClassName)
            .addParameters(parameterList)
            .beginControlFlow(
                "val result = %T(%T.Main).%T",
                scopeClassName,
                dispatchersClassName,
                asyncClassName
            )

        if (paramType == ParamType.MAP) {
            function.addStatement("val params = %T()", params)
            for (parameter in methodParameters) {
                val parameterName = parameter.simpleName.toString()
                function.addStatement("params[%S] = $parameterName", parameterName)
            }
        }

        function
            .beginControlFlow(
                "%T<%T>",
                suspendCoroutineClassName,
                resultClassName
            )
            .beginControlFlow(
                "val callback = object : %T ",
                callbackClassName
            )

        createCallback(function)

        function.endControlFlow()
            .addStatement("%T.instance", receiverClassName)
            .addStatement("  .getChannel()")
        when (paramType) {
            ParamType.MAP -> {
                function.addStatement("  ?.invokeMethod(%S, params, callback)", orgMethodName)
            }
            ParamType.ORIGIN -> {
                val variableElement = methodParameters[0]
                function.addStatement(
                    "  ?.invokeMethod(%S, ${variableElement.simpleName}, callback)",
                    orgMethodName
                )
            }
        }
        function.endControlFlow()
            .endControlFlow()
            .addStatement("return result")

        return function
    }
}