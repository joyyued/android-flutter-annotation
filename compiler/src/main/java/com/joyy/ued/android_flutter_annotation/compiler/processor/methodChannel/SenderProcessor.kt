package com.joyy.ued.android_flutter_annotation.compiler.processor.methodChannel

import com.joyy.ued.android_flutter_annotation.annotation.common.Send
import com.joyy.ued.android_flutter_annotation.annotation.method.FlutterMethodChannel
import com.joyy.ued.android_flutter_annotation.compiler.Printer
import com.joyy.ued.android_flutter_annotation.compiler.base.BaseProcessor
import com.joyy.ued.android_flutter_annotation.compiler.config.ClazzConfig
import com.joyy.ued.android_flutter_annotation.compiler.utils.ParamType
import com.joyy.ued.android_flutter_annotation.compiler.utils.ProcessorHelper
import com.joyy.ued.android_flutter_annotation.compiler.utils.TypeChangeUtils
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
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/30 10:24 上午
 * @email: 56002982@qq.com
 * @des: Method Channel 发送者
 */
class SenderProcessor(
    printer: Printer,
    processingEnv: ProcessingEnvironment,
    roundEnv: RoundEnvironment
) : BaseProcessor(
    printer,
    processingEnv,
    roundEnv
) {

    // scope
    private val scopeClassName = ClassName(
        ClazzConfig.Coroutine.COROUTINE_X_PACKAGE,
        ClazzConfig.Coroutine.COROUTINE_SCOPE_NAME
    )

    // Dispatchers
    private val dispatchersClassName = ClassName(
        ClazzConfig.Coroutine.COROUTINE_X_PACKAGE,
        ClazzConfig.Coroutine.COROUTINE_DISPATCHERS_NAME
    )

    // async
    private val asyncClassName = ClassName(
        ClazzConfig.Coroutine.COROUTINE_X_PACKAGE,
        ClazzConfig.Coroutine.COROUTINE_ASYNC_NAME
    )

    // MethodChannelResult
    private val resultClassName = ClassName(
        ClazzConfig.METHOD_RESULT_MODEL_PACKAGE,
        ClazzConfig.METHOD_RESULT_NAME
    )

    // SuccessResult
    private val successClassName = ClassName(
        ClazzConfig.METHOD_RESULT_MODEL_PACKAGE,
        ClazzConfig.METHOD_RESULT_SUCCESS_NAME
    )

    // ErrorResult
    private val errorClassName = ClassName(
        ClazzConfig.METHOD_RESULT_MODEL_PACKAGE,
        ClazzConfig.METHOD_RESULT_ERROR_NAME
    )

    // MethodChannelResultType
    private val typeClassName = ClassName(
        ClazzConfig.METHOD_RESULT_MODEL_PACKAGE,
        ClazzConfig.METHOD_RESULT_TYPE_NAME
    )

    // Result
    private val callbackClassName = ClassName(
        ClazzConfig.Flutter.METHOD_RESULT_PACKAGE,
        ClazzConfig.Flutter.METHOD_RESULT_NAME
    )

    // suspendCoroutine
    private val suspendCoroutineClassName = ClassName(
        ClazzConfig.Coroutine.COROUTINE_PACKAGE,
        ClazzConfig.Coroutine.COROUTINE_SUSPEND_COROUTINE_NAME
    )

    // resume
    private val resumeClassName = ClassName(
        ClazzConfig.Coroutine.COROUTINE_PACKAGE,
        ClazzConfig.Coroutine.COROUTINE_RESUME_NAME
    )

    // Deferred
    private val deferredClassName = ClassName(
        ClazzConfig.Coroutine.COROUTINE_X_PACKAGE,
        ClazzConfig.Coroutine.DEFERRED_NAME
    ).parameterizedBy(
        resultClassName
    )

    // MethodChannel
    private val methodChannelClassName = ClassName(
        ClazzConfig.Flutter.METHOD_CHANNEL_PACKAGE,
        ClazzConfig.Flutter.METHOD_CHANNEL_NAME
    ).copy(nullable = true)

    // BaseSenderChannel
    private val baseSenderChannelClassName = ClassName(
        ClazzConfig.PACKAGE.BASE_NAME,
        ClazzConfig.BASE_SENDER_CHANNEL_NAME
    )

    fun handle(
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
                        method
                    )
                )
            }
        }

        val releaseFunction = FunSpec.builder("release")
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("channel = null")
            .build()
        functions.add(releaseFunction)

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
                    .mutable(true)
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
            .addSuperinterface(baseSenderChannelClassName)

        FileSpec.get(ClazzConfig.PACKAGE.CHANNEL_NAME, engineCreatorClazzBuilder.build())
            .writeTo(filer)
    }

    private fun assembleFunction(
        method: ExecutableElement
    ): ArrayList<FunSpec> {

        val senderAnnotation = method.getAnnotation(Send::class.java)
        val methodName = method.simpleName.toString()
        val parameters = method.parameters

        // 获取参数类型
        val paramType = ProcessorHelper.checkParam(printer, method, parameters)

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

        val senderName = senderAnnotation?.name ?: methodName

        // 创建方法
        val list = ArrayList<FunSpec>()
        val function = createMethod(
            orgMethodName = methodName,
            senderName = senderName,
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
        orgMethodName: String,
        senderName: String,
        paramType: ParamType,
        methodParameters: List<VariableElement>,
        parameterList: List<ParameterSpec>
    ): FunSpec.Builder {

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
            .addStatement(statement, senderName)
            .endControlFlow()
            .endControlFlow()
            .addStatement("return result")

        return function
    }
}