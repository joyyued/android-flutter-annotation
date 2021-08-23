package com.joyy.neza_compiler.processor.basicChannel

import com.joyy.neza_annotation.basic.FlutterBasicChannel
import com.joyy.neza_compiler.Printer
import com.joyy.neza_compiler.config.ClazzConfig
import com.joyy.neza_compiler.utils.ParamType
import com.joyy.neza_compiler.utils.ProcessorHelper
import com.joyy.neza_compiler.utils.TypeChangeUtils
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
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
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types

class SenderProcessor(
    private val filer: Filer,
    private val printer: Printer,
    private val typeUtils: Types
) {

    private val scopeClassName = ClassName(
        ClazzConfig.Coroutine.COROUTINE_X_PACKAGE,
        ClazzConfig.Coroutine.COROUTINE_SCOPE_NAME,
    )
    private val dispatchersClassName = ClassName(
        ClazzConfig.Coroutine.COROUTINE_X_PACKAGE,
        ClazzConfig.Coroutine.COROUTINE_DISPATCHERS_NAME,
    )
    private val asyncClassName = ClassName(
        ClazzConfig.Coroutine.COROUTINE_X_PACKAGE,
        ClazzConfig.Coroutine.COROUTINE_ASYNC_NAME,
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
    )

    fun handle(
        roundEnv: RoundEnvironment,
        element: Element,
        channelReceiverMap: HashMap<String, ChannelInfo>
    ) {
        val clazzName = element.simpleName
        val generateClazzName = "${clazzName}Impl"
        val channelAnnotation = element.getAnnotation(FlutterBasicChannel::class.java)
        val channelName = channelAnnotation.channelName

        val kind = element.kind
        if (kind != ElementKind.INTERFACE) {
            printer.error("The sender of Method channel must be a interface.[$clazzName]")
            return
        }

        val codecTypeMirror = BasicProcessorUtils.getCodecTypeMirror(channelAnnotation)
        if (codecTypeMirror == null) {
            printer.error("Can not get codec.Check the codec is exist.")
            return
        }

        val genericsType = BasicProcessorUtils.getGenericsType(codecTypeMirror, printer)
        if (genericsType == null) {
            printer.error("You must support a generic in codec.")
            return
        }

        // 生成接收类名
        if (channelReceiverMap[channelName] == null) {
            channelReceiverMap[channelName] = ChannelInfo(
                className = ClassName(
                    ClazzConfig.PACKAGE.NEZA_CHANNEL,
                    "${clazzName}Proxy"
                ),
                typeMirror = genericsType
            )
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
        method: ExecutableElement,
        channelName: String,
        channelReceiverMap: HashMap<String, ChannelInfo>
    ): ArrayList<FunSpec> {
        val list = ArrayList<FunSpec>()
        val receiverChannelInfo = channelReceiverMap[channelName]
        if (receiverChannelInfo == null) {
            printer.error(
                "Receiver is null. " +
                        "You may try again or check your basic message channel code is valid." +
                        "[ $method ]"
            )
            return list
        }

        val parameters = method.parameters
        val paramType = ProcessorHelper.checkParam(printer, method)

        checkParam(
            method = method,
            paramType = paramType,
            parameters = parameters,
            receiverChannelInfo = receiverChannelInfo
        )

        return assembleFunction(
            method,
            paramType,
            parameters,
            receiverChannelInfo.className,
            receiverChannelInfo.typeMirror
        )
    }

    private fun checkParam(
        method: ExecutableElement,
        paramType: ParamType,
        parameters: List<VariableElement>,
        receiverChannelInfo: ChannelInfo
    ) {
        when (paramType) {
            ParamType.ORIGIN -> {
                val typeMirror = receiverChannelInfo.typeMirror
                val typeName = typeMirror.asTypeName().toString()

                if (typeName != Object::class.java.canonicalName
                    && typeName != Any::class.qualifiedName
                    && !typeUtils.isSameType(parameters[0].asType(), typeMirror)
                ) {
                    printer.error(
                        "The parameter type is not same to the basic message.[$method]|" +
                                "${parameters[0].asType()} |" +
                                "${receiverChannelInfo.typeMirror}"
                    )
                }
            }
            ParamType.MAP -> {
                val receiverTypeName = receiverChannelInfo.typeMirror.asTypeName()

                if (receiverTypeName != Object::class.java.asTypeName()
                    && receiverTypeName != Any::class.asTypeName()
                    && receiverTypeName != HashMap::class.asTypeName()
                    && receiverTypeName != Map::class.asTypeName()
                ) {
                    printer.error(
                        "Can't transform the HashMap type to $receiverTypeName in " +
                                "${method.simpleName} function. " +
                                "You can try use one parameter which is the type is " +
                                "$receiverTypeName, and add a @Param annotation for it."
                    )
                }
            }
        }
    }

    private fun assembleFunction(
        method: ExecutableElement,
        paramType: ParamType,
        parameters: List<VariableElement>,
        receiverClassName: ClassName,
        type: TypeMirror
    ): ArrayList<FunSpec> {
        val list = ArrayList<FunSpec>()

        val type = TypeChangeUtils.change(type.asTypeName()).copy(nullable = true)
        val methodName = method.simpleName.toString()

        val hashMapClassName = HashMap::class.asClassName().parameterizedBy(
            String::class.asTypeName(),
            Any::class.asClassName().copy(nullable = true)
        )

        val function = FunSpec.builder(methodName)
            .returns(deferredClassName.parameterizedBy(type))
            .beginControlFlow(
                "val result = %T(%T.Main).%T",
                scopeClassName,
                dispatchersClassName,
                asyncClassName
            )

        if (paramType == ParamType.MAP) {
            function.addStatement("val params = %T()", hashMapClassName)
        }

        val parameterList = ArrayList<ParameterSpec>()
        for (parameter in parameters) {
            val parameterName = parameter.simpleName.toString()

            var parameterType = parameter.asType().asTypeName()
            parameterType = TypeChangeUtils.change(parameterType)

            val nullableAnnotation = parameter.getAnnotation(Nullable::class.java)
            if (nullableAnnotation != null) {
                parameterType = parameterType.copy(nullable = true)
            }
            parameterList.add(
                ParameterSpec.builder(
                    parameterName,
                    parameterType
                ).build()
            )

            if (paramType == ParamType.MAP) {
                function.addStatement("params[%S] = $parameterName", parameterName)
            }
        }

        function.addParameters(parameterList)

        var log = ""
        val paramName = when (paramType) {
            ParamType.ORIGIN -> {
                if (parameters.isEmpty()) {
                    "Any()"
                } else if (parameters.size == 1) {
                    parameters[0].simpleName
                } else {
                    log = "You use @Param annotation on multi parameters function." +
                            "This caused only the first parameter will be used."
                    printer.warning("$log [ $methodName ] ")
                    parameters[0].simpleName
                }
            }
            ParamType.MAP -> {
                "params"
            }
        }

        function
            .beginControlFlow(
                "%T<%T>",
                suspendCoroutineClassName,
                type
            )
            .addStatement(
                "val callback = %T.Reply<%T> { reply ->",
                ClassName(
                    ClazzConfig.Flutter.METHOD_CHANNEL_PACKAGE,
                    ClazzConfig.Flutter.BASIC_CHANNEL_NAME,
                ),
                type
            )
            .addStatement("  it.%T(reply)", resumeClassName)
            .addStatement("}")

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

        function.addStatement("%T.instance", receiverClassName)
            .addStatement("  .getChannel()")
            .addStatement("  ?.send($paramName, callback)")
            .endControlFlow()
            .endControlFlow()
            .addStatement("return result")
        list.add(function.build())

        return list
    }
}