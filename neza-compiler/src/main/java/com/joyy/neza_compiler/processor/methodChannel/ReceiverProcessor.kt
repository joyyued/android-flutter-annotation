package com.joyy.neza_compiler.processor.methodChannel

import com.joyy.neza_annotation.common.Callback
import com.joyy.neza_annotation.FlutterEngine
import com.joyy.neza_annotation.method.FlutterMethodChannel
import com.joyy.neza_annotation.method.HandleMessage
import com.joyy.neza_annotation.method.ParseData
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
import java.util.Locale
import javax.annotation.processing.Filer
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/12 11:39 上午
 * @email: 56002982@qq.com
 * @des: 接收者处理器
 */
class ReceiverProcessor(
    private var elementUtils: Elements,
    private var typeUtils: Types,
    private val filer: Filer,
    private val printer: Printer
) {

    fun handle(
        roundEnv: RoundEnvironment,
        element: TypeElement,
        channelReceiverMap: HashMap<String, ClassName>,
        isLackCreator: Boolean = false
    ) {

        val clazzName = element.simpleName
        val generateClazzName = "${clazzName}Proxy"
        val channelAnnotation = element.getAnnotation(FlutterMethodChannel::class.java)
        val engineAnnotation = element.getAnnotation(FlutterEngine::class.java)
        val channelName = channelAnnotation.channelName

//        printer.note("receiver kind: ${element.kind}")

        val funList = ArrayList<FunSpec>()
        val propertyList = ArrayList<PropertySpec>()

        // private val engineId = "NEZA_ENGINE_ID"
        val engineId = engineAnnotation?.engineId ?: EngineHelper.getEngineId(roundEnv)
        val engineIdProperty = PropertySpec.builder("engineId", String::class)
            .addModifiers(KModifier.PRIVATE)
            .initializer("%S", engineId)
            .build()
        propertyList.add(engineIdProperty)

        // private val name = "com.zinc.android_flutter_annotation/nezaMethodChannel"
        val nameProperty = PropertySpec.builder("name", String::class)
            .addModifiers(KModifier.PRIVATE)
            .initializer("%S", channelName)
            .build()
        propertyList.add(nameProperty)

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
        propertyList.add(flutterEngineProperty)

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
        propertyList.add(channelProperty)

        //  private val nezaMethodChannel:NezaMethodChannel = NezaMethodChannel()
        val methodChannelName = element.simpleName.toString().replaceFirstChar {
            it.lowercase(Locale.getDefault())
        }
        if (!isLackCreator) {
            val methodChannelProperty = PropertySpec.builder(
                methodChannelName,
                element.asType().asTypeName()
            ).addModifiers(KModifier.PRIVATE)
                .initializer("%T()", element)
                .build()
            propertyList.add(methodChannelProperty)
        }

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
        if (!isLackCreator) {
            assembleResultField(methodChannelName, element, initFun)
        }
        initFun.addStatement("  val method = call.method")
            .addStatement("  val arguments = call.arguments")
            .addStatement("  when (method) {")
        if (!isLackCreator) {
            // 拼装方法
            assembleMethod(methodChannelName, element, initFun)
        }
        initFun.addStatement("  }")
            .addStatement("}")
            .endControlFlow()
        funList.add(initFun.build())

        val getChannelFun = FunSpec.builder("getChannel")
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return channel")
            .build()
        funList.add(getChannelFun)

        val getChannelNameFun = FunSpec.builder("getChannelName")
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return name")
            .build()
        funList.add(getChannelNameFun)

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
            .addProperties(propertyList)
            .addFunctions(funList)
            .build()

        FileSpec.get(ClazzConfig.PACKAGE.NEZA_CHANNEL, engineCreatorClazz)
            .writeTo(filer)

        channelReceiverMap[channelName] = ClassName(
            ClazzConfig.PACKAGE.NEZA_CHANNEL,
            generateClazzName
        )
    }

    private fun assembleResultField(
        methodChannelName: String,
        element: TypeElement,
        initFun: FunSpec.Builder
    ) {
        val enclosedElements = element.enclosedElements
        val resultPath = ClazzConfig.Flutter.METHOD_RESULT_PACKAGE +
                "." +
                ClazzConfig.Flutter.METHOD_RESULT_NAME

        val resultElements = ArrayList<VariableElement>()
        for (item in enclosedElements) {
            if (item !is VariableElement) {
                continue
            }

            if (item.getAnnotation(Callback::class.java) == null) {
                continue
            }

            val type = item.asType()?.toString()
            if (type != resultPath) {
                printer.error(
                    "The parameter must be a $resultPath type if you use @Callback." +
                            "[$element -- $item]"
                )
                return
            }
            resultElements.add(item)
        }

        for (resultElement in resultElements) {
            initFun.addStatement("  $methodChannelName.${resultElement.simpleName} = result")
        }
    }

    private fun assembleMethod(
        methodChannelName: String,
        element: TypeElement,
        initFun: FunSpec.Builder
    ) {
        val enclosedElements = element.enclosedElements

        val fieldElements = ArrayList<VariableElement>()
        for (item in enclosedElements) {
            if (item !is VariableElement) {
                continue
            }
            fieldElements.add(item)
//            DebugUtils.showPropertyInfo(printer, item)
        }

        val methodList = ArrayList<ExecutableElement>()
        for (item in enclosedElements) {
            if (item !is ExecutableElement) {
                continue
            }

            var isSkip = false
            for (fieldElement in fieldElements) {
                var fieldName = fieldElement.simpleName.toString()
                fieldName = fieldName.replaceFirstChar { it.uppercase(Locale.getDefault()) }
                val fieldType = fieldElement.asType()

                val methodName = item.simpleName.toString()
                val parameters = item.parameters
                if (methodName == "set${fieldName}") {
                    if (parameters.size == 1) {
                        val parameter = parameters[0]
                        if (typeUtils.isSameType(parameter.asType(), fieldType)) {
                            isSkip = true
                            break
                        }
                    }
                } else if (methodName == "get${fieldName}") {
                    if (parameters.size == 0) {
                        if (typeUtils.isSameType(item.returnType, fieldType)) {
                            isSkip = true
                            break
                        }
                    }
                }
            }

            if (isSkip) {
                continue
            }

            methodList.add(item)
//            DebugUtils.showMethodInfo(printer, item)
        }

        assembleMethod(
            methodChannelName = methodChannelName,
            element = element,
            methodList = methodList,
            initFun = initFun,
        )
    }

    private fun assembleMethod(
        methodChannelName: String,
        element: Element,
        methodList: List<ExecutableElement>,
        initFun: FunSpec.Builder,
    ) {
        val spacing = "    "
        val methodNameSet = HashSet<String>()
        for (method in methodList) {
            if (method.kind != ElementKind.METHOD) {
                continue
            }

            val methodName = method.simpleName
            val parameters = method.parameters

            if (methodNameSet.contains(methodName.toString())) {
                printer.error(
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

            method.getAnnotation(HandleMessage::class.java) ?: continue

            // "sayHelloToNative" -> {
            val block = initFun.addStatement("$spacing%S -> {", methodName)
            if (parameters.isEmpty()) {   // 没有参数
                block.addStatement("$spacing  $methodChannelName.$methodName()")
            } else {    // 构建参数
                block.addStatement("$spacing  $methodChannelName.$methodName(")
                val parseDataAnnotation = method.getAnnotation(ParseData::class.java)

                if (parseDataAnnotation != null) {
                    for (parameter in parameters) {
                        parameter ?: continue

                        val paramName = parameter.simpleName
                        val paramType = parameter.asType()
                        val nullableAnnotation = parameter.getAnnotation(Nullable::class.java)

                        val type = TypeChangeUtils.change(paramType.toString())

                        if (nullableAnnotation == null) {
                            printer.error(
                                "Parameter must be nullable " +
                                        "when you use @ParseData annotation. [$methodName]"
                            )
                            return
                        }
                        block.addStatement(
                            "$spacing    $paramName = call.argument<$type>(\"$paramName\"),",
                        )
                    }
                } else if (parameters.size == 1) {
                    val parameter = parameters[0]
                    val type =
                        TypeChangeUtils.change(parameter.asType().asTypeName().toString())
                    val name = parameter.simpleName.toString()
                    if (type != "Any") {
                        printer.error(
                            "Only Any type can be use in one parameter function. [$methodName]"
                        )
                    }
                    block.addStatement(
                        "$spacing    $name = arguments",
                    )
                } else {
                    printer.error(
                        "There are more than one parameter in $methodName function." +
                                "You have two choice:\n" +
                                "1、Change the parameters size and the parameter must be a Any type\n" +
                                "2、User @ParseData annotation on this method, we'll parse the data " +
                                "for you，while it may be cause some error if you use the complex type."
                    )
                }

                block.addStatement("$spacing  )")
            }
            block.addStatement("$spacing}")
            methodNameSet.add(methodName.toString())
        }
    }
}