package com.joyy.neza_compiler.processor.methodChannel

import com.joyy.neza_annotation.FlutterEngine
import com.joyy.neza_annotation.method.FlutterMethodChannel
import com.joyy.neza_annotation.method.RawData
import com.joyy.neza_compiler.Printer
import com.joyy.neza_compiler.config.ClazzConfig
import com.joyy.neza_compiler.utils.EngineHelper
import com.joyy.neza_compiler.utils.TypeChangeUtils
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.annotations.Nullable
import javax.annotation.processing.Filer
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/12 11:39 上午
 * @email: 56002982@qq.com
 * @des: 接收者处理器
 */
class ReceiverProcessor(
    private val filer: Filer,
    private val printer: Printer
) {

    fun handle(
        roundEnv: RoundEnvironment,
        element: Element,
        channelReceiverMap: HashMap<String, ClassName>
    ) {
        val clazzName = element.simpleName
        val generateClazzName = "${clazzName}Proxy"
        val channelAnnotation = element.getAnnotation(FlutterMethodChannel::class.java)
        val engineAnnotation = element.getAnnotation(FlutterEngine::class.java)
        val channelName = channelAnnotation.channelName

        printer.note("receiver kind: ${element.kind}")

        // private val engineId = "NEZA_ENGINE_ID"
        val engineId = engineAnnotation?.engineId ?: EngineHelper.getEngineId(roundEnv)
        val engineIdProperty = PropertySpec.builder("engineId", String::class)
            .addModifiers(KModifier.PRIVATE)
            .initializer("%S", engineId)
            .build()

        // private val name = "com.zinc.android_flutter_annotation/nezaMethodChannel"
        val nameProperty = PropertySpec.builder("name", String::class)
            .addModifiers(KModifier.PRIVATE)
            .initializer("%S", channelName)
            .build()

        // private var engine: FlutterEngine? = null
        val engineClassName = ClassName(
            ClazzConfig.Flutter.ENGINE_PACKAGE,
            ClazzConfig.Flutter.ENGINE_NAME,
        )
        val flutterEngineProperty = PropertySpec.builder(
            "engine",
            engineClassName.copy(nullable = true)
        ).mutable()
            .addModifiers(KModifier.PRIVATE)
            .initializer("null")
            .build()

        // private var channel: MethodChannel? = null
        val channelClassName = ClassName(
            ClazzConfig.Flutter.METHOD_CHANNEL_PACKAGE,
            ClazzConfig.Flutter.METHOD_CHANNEL_NAME,
        )
        val channelProperty = PropertySpec.builder(
            "channel",
            channelClassName.copy(nullable = true)
        ).mutable()
            .addModifiers(KModifier.PRIVATE)
            .initializer("null")
            .build()

        val contextClassName = ClassName(
            ClazzConfig.Android.CONTEXT_PACKAGE,
            ClazzConfig.Android.CONTEXT_NAME,
        )
        val engineHelperClassName = ClassName(
            ClazzConfig.ENGINE_HELPER_PACKAGE,
            ClazzConfig.ENGINE_HELPER_NAME,
        )

        val initFun = FunSpec.builder("init")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("context", contextClassName)
            .beginControlFlow(
                "engine = %T.getFlutterEngine(engineId)?.apply",
                engineHelperClassName
            )
            .addStatement("channel = MethodChannel(")
            .addStatement("  dartExecutor.binaryMessenger,")
            .addStatement("  name")
            .addStatement(")")
            .addStatement("channel?.setMethodCallHandler { call, result ->")
            .addStatement("  val method = call.method")
            .addStatement("  val arguments = call.arguments")
            .addStatement("  when (method) {")
        // 拼装方法
        assembleMethod(element, initFun)
        initFun.addStatement("  }")
            .addStatement("}")
            .endControlFlow()

        val getChannelFun = FunSpec.builder("getChannel")
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return channel")
            .build()

        val getChannelNameFun = FunSpec.builder("getChannelName")
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return name")
            .build()

        val companion = TypeSpec.companionObjectBuilder()
            .addProperty(
                PropertySpec
                    .builder(
                        "instance",
                        ClassName(
                            ClazzConfig.PACKAGE.NEZA_CHANNEL,
                            generateClazzName
                        )
                    )
                    .delegate(
                        CodeBlock.builder()
                            .beginControlFlow(
                                "lazy(mode = %T.SYNCHRONIZED)",
                                LazyThreadSafetyMode::class.asTypeName()
                            )
                            .add("$generateClazzName()")
                            .endControlFlow()
                            .build()
                    )
                    .build()
            )
            .build()

        // MethodChannelInterface
        val methodChannelInterface = ClassName(
            ClazzConfig.Channel.CHANNEL_PACKAGE,
            ClazzConfig.Channel.METHOD_CHANNEL_NAME,
        )
        val engineCreatorClazz = TypeSpec.classBuilder(generateClazzName)
            .addSuperinterface(methodChannelInterface)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
            .addType(companion)
            .addProperty(engineIdProperty)
            .addProperty(nameProperty)
            .addProperty(flutterEngineProperty)
            .addProperty(channelProperty)
            .addFunction(initFun.build())
            .addFunction(getChannelFun)
            .addFunction(getChannelNameFun)
            .build()

        FileSpec.get(ClazzConfig.PACKAGE.NEZA_CHANNEL, engineCreatorClazz)
            .writeTo(filer)

        channelReceiverMap[channelName] = ClassName(
            ClazzConfig.PACKAGE.NEZA_CHANNEL,
            generateClazzName
        )
    }

    private fun assembleMethod(element: Element, initFun: FunSpec.Builder) {
        val spacing = "    "
        val enclosedElements = (element as TypeElement).enclosedElements
        val methodNameSet = HashSet<String>()
        for (method in enclosedElements) {
            if (method !is ExecutableElement) {
                continue
            }
            if (method.kind != ElementKind.METHOD) {
                continue
            }

            val methodName = method.simpleName
            val parameters = method.parameters

            if (methodNameSet.contains(methodName.toString())) {
                error(
                    "Method name[$methodName] already exists in $element." +
                            "You should use different method name in this class."
                )
            }

            printer.note(
                "[${ClazzConfig.PROJECT_NAME}] method: $methodName |" +
                        " ${method.asType()} |" +
                        " ${method.kind} |" +
                        " ${method.parameters} |" +
                        " ${method.typeParameters}"
            )

            // "sayHelloToNative" -> {
            val block = initFun.addStatement("$spacing%S -> {", methodName)
            if (parameters.isEmpty()) {   // 没有参数
                block.addStatement("$spacing  %T.$methodName()", element)
            } else {    // 构建参数
                block.addStatement("$spacing  %T.$methodName(", element)
                for (parameter in parameters) {
                    parameter ?: continue

                    val paramName = parameter.simpleName
                    val paramType = parameter.asType()
                    val rawDataAnnotation = parameter.getAnnotation(RawData::class.java)
                    val nullableAnnotation = parameter.getAnnotation(Nullable::class.java)

//                    printer.note(
//                        "params: $parameter |" +
//                                " ${parameter.constantValue} |" +
//                                " $paramType |" +
//                                " $rawDataAnnotation |" +
//                                " $nullableAnnotation |" +
//                                " $paramName |" +
//                                " ${parameter.modifiers}"
//                    )

                    val type = TypeChangeUtils.change(paramType.toString())
                    if (rawDataAnnotation != null) {
                        if (type != "Any") {
                            error("Only Any type parameter can use @RawData.")
                        }
                        block.addStatement(
                            "$spacing    $paramName = arguments",
                        )
                    } else {
                        if (nullableAnnotation == null) {
                            error(
                                "Parameter must be nullable unless use @RawData to the Any type" +
                                        "parameter.[$methodName]"
                            )
                        }
                        block.addStatement(
                            "$spacing    $paramName = call.argument<$type>(\"$paramName\")",
                        )
                    }
                }
                block.addStatement("$spacing  )")
            }
            block.addStatement("$spacing}")
            methodNameSet.add(methodName.toString())
        }
    }
}