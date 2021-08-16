package com.joyy.neza_compiler.processor.basicChannel

import com.joyy.neza_annotation.basic.FlutterBasicChannel
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
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types

class SenderProcessor(
    private val filer: Filer,
    private val printer: Printer,
    private val typeUtils: Types
) {

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
        channelReceiverMap: HashMap<String, ChannelInfo>
    ): ArrayList<FunSpec> {
        val list = ArrayList<FunSpec>()
        val receiverChannelInfo = channelReceiverMap[channelName]
        if (receiverChannelInfo == null) {
            printer.error("[Sender] Receiver is null.")
            return list
        }

        val parameters = method.parameters
        if (parameters.size == 1) {
            if (!typeUtils.isSameType(
                    parameters[0].asType(),
                    receiverChannelInfo.typeMirror
                )
            ) {
                printer.error("The parameter type is not same to the basic message.")
                return list
            }
            return assembleSingleParameterFun(
                method,
                parameters[0],
                receiverChannelInfo.className
            )
        } else {
            if (receiverChannelInfo.typeMirror.asTypeName() != HashMap::class.asTypeName()) {
                printer.error(
                    "The basic message type must be HashMap, " +
                            "when you use more then one parameter or none parameter."
                )
                return list
            }
            return assembleMultiOrNoneParameterFun(
                method,
                parameters,
                receiverChannelInfo.className,
                receiverChannelInfo.typeMirror
            )
        }
    }

    private fun assembleSingleParameterFun(
        method: ExecutableElement,
        parameter: VariableElement,
        receiverClassName: ClassName,
    ): ArrayList<FunSpec> {
        val list = ArrayList<FunSpec>()
        val methodName = method.simpleName.toString()
        val parameterName = parameter.simpleName.toString()
        val parameterType = TypeChangeUtils.change(parameter.asType().asTypeName())
        val syncFun = FunSpec.builder(methodName)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter(parameterName, parameterType)
            .beginControlFlow(
                "%T(%T.Main).%T",
                scopeClassName,
                dispatchersClassName,
                launchClassName
            )
            .addStatement("${methodName}Async($parameterName)")
            .endControlFlow()
            .build()
        list.add(syncFun)

        val asyncFun = FunSpec.builder("${methodName}Async")
            .addModifiers(KModifier.SUSPEND)
            .addParameter(parameterName, parameterType)
            .returns(parameterType.copy(nullable = true))
            .beginControlFlow(
                "return %T",
                suspendCoroutineClassName
            )
            .addStatement(
                "val callback = %T.Reply<%T> { reply ->",
                ClassName(
                    ClazzConfig.Flutter.METHOD_CHANNEL_PACKAGE,
                    ClazzConfig.Flutter.Basic_CHANNEL_NAME,
                ),
                parameterType
            )
            .addStatement("  it.%T(reply)", resumeClassName)
            .addStatement("}")
            .addStatement("%T.instance", receiverClassName)
            .addStatement("  .getChannel()")
            .addStatement("  ?.send($parameterName, callback)")
            .endControlFlow()
            .build()
        list.add(asyncFun)

        return list
    }

    private fun assembleMultiOrNoneParameterFun(
        method: ExecutableElement,
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

        val syncFun = FunSpec.builder(methodName)
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("val params = %T()", hashMapClassName)

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
            syncFun.addStatement("params[%S] = $parameterName", parameterName)
        }
        syncFun.addParameters(parameterList)
            .beginControlFlow(
                "%T(%T.Main).%T",
                scopeClassName,
                dispatchersClassName,
                launchClassName
            )
            .addStatement("${methodName}Async(params)")
            .endControlFlow()
        list.add(syncFun.build())

        val asyncFun = FunSpec.builder("${methodName}Async")
            .addModifiers(KModifier.SUSPEND)
            .addParameter("params", hashMapClassName)
            .returns(type)
            .beginControlFlow(
                "return %T",
                suspendCoroutineClassName
            )
            .addStatement(
                "val callback = %T.Reply<%T> { reply ->",
                ClassName(
                    ClazzConfig.Flutter.METHOD_CHANNEL_PACKAGE,
                    ClazzConfig.Flutter.Basic_CHANNEL_NAME,
                ),
                type
            )
            .addStatement("  it.%T(reply)", resumeClassName)
            .addStatement("}")
            .addStatement("%T.instance", receiverClassName)
            .addStatement("  .getChannel()")
            .addStatement("  ?.send(params, callback)")
            .endControlFlow()
            .build()
        list.add(asyncFun)

        return list
    }
}