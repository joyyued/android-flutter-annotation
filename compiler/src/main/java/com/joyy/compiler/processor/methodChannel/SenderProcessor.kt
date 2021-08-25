package com.joyy.compiler.processor.methodChannel

import com.joyy.annotation.method.FlutterMethodChannel
import com.joyy.compiler.Printer
import com.joyy.compiler.config.ClazzConfig
import com.joyy.compiler.utils.ParamType
import com.joyy.compiler.utils.ProcessorHelper
import com.joyy.compiler.utils.TypeChangeUtils
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
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
        ClazzConfig.Coroutine.COROUTINE_SUSPEND_COROUTINE_NAME
    )
    private val resumeClassName = ClassName(
        ClazzConfig.Coroutine.COROUTINE_PACKAGE,
        ClazzConfig.Coroutine.COROUTINE_RESUME_NAME
    )
    private val deferredClassName = ClassName(
        ClazzConfig.Coroutine.COROUTINE_X_PACKAGE,
        ClazzConfig.Coroutine.DEFERRED_NAME
    ).parameterizedBy(
        resultClassName
    )
    private val methodChannelClassName = ClassName(
        ClazzConfig.Flutter.METHOD_CHANNEL_PACKAGE,
        ClazzConfig.Flutter.METHOD_CHANNEL_NAME
    ).copy(nullable = true)

    fun handle(
        roundEnv: RoundEnvironment,
        element: Element,
        channelReceiverMap: HashMap<String, ClassName>
    ) {
        // 注解类名
        val clazzName = element.simpleName.toString()
        // 生成类名
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

        val engineCreatorClazzBuilder = TypeSpec
            .classBuilder(generateClazzName)
            .addFunctions(functions)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        "channel",
                        methodChannelClassName
                    )
                    .build()
            )
            .addProperty(
                PropertySpec.builder("channel", methodChannelClassName)
                    .initializer("channel")
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )

        FileSpec.get(ClazzConfig.PACKAGE.CHANNEL_NAME, engineCreatorClazzBuilder.build())
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

        // 获取参数类型
        val paramType = ProcessorHelper.checkParam(printer, method, parameters)

        // Proxy 类名
        var receiverClassName = channelReceiverMap[channelName]
        if (receiverClassName == null) {
            receiverClassName = ClassName(
                ClazzConfig.PACKAGE.CHANNEL_NAME,
                "${clazzName}Proxy"
            )
        }

        // 组装参数类型
        val parameterList = ArrayList<ParameterSpec>()
        for (parameter in parameters) {
            val typeName = parameter.asType()
            var type = TypeChangeUtils.change(printer, typeName)

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

        // 创建方法
        val list = ArrayList<FunSpec>()
        val function = createMethod(
            receiverClassName = receiverClassName,
            orgMethodName = methodName,
            paramType = paramType,
            methodParameters = parameters,
            parameterList = parameterList
        )
        list.add(function.build())

        return list
    }

    /**
     * 创建回调
     * @param function 需要添加回调的方法
     */
    private fun createCallback(function: FunSpec.Builder) {
        // callback -> success
        function.beginControlFlow("override fun success(result: Any?)")
            .addStatement("it.%T(", resumeClassName)
            .addStatement("  %T(", resultClassName)
            .addStatement("    resultType = %T.SUCCESS,", typeClassName)
            .addStatement("    successResult = %T(result)", successClassName)
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
            .addStatement("    resultType = %T.NOT_IMPLEMENTED", typeClassName)
            .addStatement("  )")
            .addStatement(")")
            .endControlFlow()
    }

    /**
     * 创建方法
     */
    private fun createMethod(
        receiverClassName: ClassName,
        orgMethodName: String,
        paramType: ParamType,
        methodParameters: List<VariableElement>,
        parameterList: List<ParameterSpec>
    ): FunSpec.Builder {
        // scope
        val scopeClassName = ClassName(
            ClazzConfig.Coroutine.COROUTINE_X_PACKAGE,
            ClazzConfig.Coroutine.COROUTINE_SCOPE_NAME
        )
        // Dispatchers
        val dispatchersClassName = ClassName(
            ClazzConfig.Coroutine.COROUTINE_X_PACKAGE,
            ClazzConfig.Coroutine.COROUTINE_DISPATCHERS_NAME
        )
        // async
        val asyncClassName = ClassName(
            ClazzConfig.Coroutine.COROUTINE_X_PACKAGE,
            ClazzConfig.Coroutine.COROUTINE_ASYNC_NAME
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

        // 创建 HashMap 类型 HashMap<String, Any?>
        val params = HashMap::class.asClassName().parameterizedBy(
            String::class.asTypeName(),
            Any::class.asClassName().copy(nullable = true)
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

        var log = ""
        val statement = when (paramType) {
            ParamType.MAP -> {
                "channel?.invokeMethod(%S, params, callback)"
            }
            ParamType.ORIGIN -> {
                val size = methodParameters.size
                val paramName = when {
                    size <= 0 -> {
                        "\"\""
                    }
                    size == 1 -> {
                        methodParameters[0].simpleName
                    }
                    else -> {
                        log = "You use @Param annotation on multi parameters function." +
                                "This caused only the first parameter will be used."
                        printer.warning("$log [ $orgMethodName ] ")
                        methodParameters[0].simpleName
                    }
                }

                "channel?.invokeMethod(%S, $paramName, callback)"
            }
        }

        if (log.isNotEmpty()) {
            function.addStatement(
                "%T.e(%S, %S)",
                ClassName(
                    ClazzConfig.Android.ANDROID_UTIL_PACKAGE,
                    ClazzConfig.Android.ANDROID_LOG_NAME
                ),
                ClazzConfig.PROJECT_NAME,
                log
            )
        }
        function
            .addStatement(statement, orgMethodName)
            .endControlFlow()
            .endControlFlow()
            .addStatement("return result")

        return function
    }
}