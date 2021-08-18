package com.joyy.neza_compiler.processor.methodChannel

import com.joyy.neza_annotation.method.FlutterMethodChannel
import com.joyy.neza_compiler.Printer
import com.joyy.neza_compiler.config.ClazzConfig
import com.joyy.neza_compiler.processor.common.ParamType
import com.joyy.neza_compiler.processor.common.ProcessorHelper
import com.joyy.neza_compiler.utils.DebugUtils
import com.joyy.neza_compiler.utils.TypeChangeUtils
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.annotations.Nullable
import sun.security.ssl.Debug
import java.lang.reflect.ParameterizedType
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
        ClazzConfig.Coroutine.COROUTINE_SUSPEND_COROUTINE_NAME,
    )
    private val resumeClassName = ClassName(
        ClazzConfig.Coroutine.COROUTINE_PACKAGE,
        ClazzConfig.Coroutine.COROUTINE_RESUME_NAME,
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
        val mapAsyncFunctions = HashSet<String>()
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
                        channelReceiverMap,
                        mapAsyncFunctions
                    )
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
        channelReceiverMap: HashMap<String, ClassName>,
        mapAsyncFunctions: HashSet<String>
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
            var type = parameter.asType().asTypeName()

            DebugUtils.showInfo(printer, parameter)
            DebugUtils.showInfo(printer, type)

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

        val params = HashMap::class.asClassName().parameterizedBy(
            String::class.asTypeName(),
            Any::class.asClassName().copy(nullable = true)
        )

        val list = ArrayList<FunSpec>()
        val syncMethod = createSyncMethod(
            asyncMethodName = asyncMethodName,
            orgMethodName = methodName,
            paramType = paramType,
            params = params,
            methodParameters = parameters,
            parameterList = parameterList
        )
        list.add(syncMethod.build())

        val asyncMethod = createAsyncMethod(
            receiverClassName = receiverClassName,
            asyncMethodName = asyncMethodName,
            orgMethodName = methodName,
            paramType = paramType,
            params = params,
            methodParameters = parameters,
            parameterList = parameterList
        )
        list.add(asyncMethod.build())

        if (paramType == ParamType.MAP && !mapAsyncFunctions.contains(asyncMethodName)) {
            val asyncMethodWithMap = createAsyncMethodWithMap(
                receiverClassName = receiverClassName,
                asyncMethodName = asyncMethodName,
                orgMethodName = methodName,
                params = params,
            )
            mapAsyncFunctions.add(asyncMethodName)
            list.add(asyncMethodWithMap.build())
        }

        return list
    }

    private fun createSyncMethod(
        asyncMethodName: String,
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

        val syncFun = FunSpec.builder(orgMethodName)
            .addModifiers(KModifier.OVERRIDE)
            .addParameters(parameterList)

        if (paramType == ParamType.MAP) {
            syncFun.addStatement("val params = %T()", params)
            for (parameter in methodParameters) {
                val parameterName = parameter.simpleName.toString()
                syncFun.addStatement("params[%S] = $parameterName", parameterName)
            }
        }

        syncFun
            .beginControlFlow(
                "%T(%T.Main).%T",
                scopeClassName,
                dispatchersClassName,
                launchClassName
            )

        when (paramType) {
            ParamType.MAP -> {
                syncFun
                    .addStatement("$asyncMethodName(params)")
                    .endControlFlow()
            }
            ParamType.ORIGIN -> {
                syncFun.addStatement("$asyncMethodName(")

                for (parameter in methodParameters) {
                    syncFun.addStatement(" ${parameter.simpleName} = ${parameter.simpleName}")
                }

                syncFun.addStatement(")")
                    .endControlFlow()
            }
        }
        return syncFun
    }

    private fun createAsyncMethodWithMap(
        receiverClassName: ClassName,
        asyncMethodName: String,
        orgMethodName: String,
        params: ParameterizedTypeName,
    ): FunSpec.Builder {
        val asyncFun = FunSpec.builder(asyncMethodName)
            .addModifiers(KModifier.SUSPEND)
            .addParameter(
                ParameterSpec
                    .builder("params", params)
                    .defaultValue("HashMap()")
                    .build()
            )
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

        createCallback(asyncFun)

        asyncFun.endControlFlow()
            .addStatement("%T.instance", receiverClassName)
            .addStatement("  .getChannel()")
            .addStatement("  ?.invokeMethod(%S, params, callback)", orgMethodName)
            .endControlFlow()
            .addStatement("return result")

        return asyncFun
    }

    private fun createAsyncMethod(
        receiverClassName: ClassName,
        asyncMethodName: String,
        orgMethodName: String,
        paramType: ParamType,
        params: ParameterizedTypeName,
        methodParameters: List<VariableElement>,
        parameterList: List<ParameterSpec>,
    ): FunSpec.Builder {

        val asyncFun = FunSpec.builder(asyncMethodName)
            .addModifiers(KModifier.SUSPEND)
            .addParameters(parameterList)
            .returns(resultClassName)

        if (paramType == ParamType.MAP) {
            asyncFun.addStatement("val params = %T()", params)
            for (parameter in methodParameters) {
                val parameterName = parameter.simpleName.toString()
                asyncFun.addStatement("params[%S] = $parameterName", parameterName)
            }
            asyncFun.addStatement("return $asyncMethodName(params)")
            return asyncFun
        }

        asyncFun
            .beginControlFlow(
                "val result = %T<%T>",
                suspendCoroutineClassName,
                resultClassName
            )
            .beginControlFlow(
                "val callback = object : %T ",
                callbackClassName
            )

        createCallback(asyncFun)

        asyncFun.endControlFlow()
            .addStatement("%T.instance", receiverClassName)
            .addStatement("  .getChannel()")

        when (paramType) {
            ParamType.MAP -> {
                asyncFun.addStatement("  ?.invokeMethod(%S, params, callback)", orgMethodName)
            }
            ParamType.ORIGIN -> {
                val variableElement = methodParameters[0]
                asyncFun.addStatement(
                    "  ?.invokeMethod(%S, ${variableElement.simpleName}, callback)",
                    orgMethodName
                )
            }
        }
        asyncFun.endControlFlow()
            .addStatement("return result")

        return asyncFun
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
}