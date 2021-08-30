package com.joyy.ued.android_flutter_annotation.compiler.processor.methodChannel

import com.joyy.ued.android_flutter_annotation.annotation.FlutterEngine
import com.joyy.ued.android_flutter_annotation.annotation.common.Callback
import com.joyy.ued.android_flutter_annotation.annotation.method.FlutterMethodChannel
import com.joyy.ued.android_flutter_annotation.annotation.common.Receive
import com.joyy.ued.android_flutter_annotation.annotation.method.ParseData
import com.joyy.ued.android_flutter_annotation.compiler.Printer
import com.joyy.ued.android_flutter_annotation.compiler.base.BaseProcessor
import com.joyy.ued.android_flutter_annotation.compiler.config.ClazzConfig
import com.joyy.ued.android_flutter_annotation.compiler.utils.EngineHelper
import com.joyy.ued.android_flutter_annotation.compiler.utils.ReceiverHelper.checkCallbackType
import com.joyy.ued.android_flutter_annotation.compiler.utils.ReceiverHelper.getParamSize
import com.joyy.ued.android_flutter_annotation.compiler.utils.ReceiverHelper.isCallback
import com.joyy.ued.android_flutter_annotation.compiler.utils.TypeChangeUtils
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
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
 * @author: Jiang PengYong
 * @date: 2021/8/12 11:39 上午
 * @email: 56002982@qq.com
 * @des: 接收者处理器
 */
class ReceiverProcessor(
    printer: Printer,
    processingEnv: ProcessingEnvironment,
    roundEnv: RoundEnvironment
) : BaseProcessor(
    printer,
    processingEnv,
    roundEnv
) {

    private val contextClassName = ClassName(
        ClazzConfig.Android.CONTEXT_PACKAGE,
        ClazzConfig.Android.CONTEXT_NAME
    )
    private val engineCacheClassName = ClassName(
        ClazzConfig.Flutter.ENGINE_PACKAGE,
        ClazzConfig.Flutter.ENGINE_CACHE_NAME
    )
    private val methodChannelClassName = ClassName(
        ClazzConfig.Flutter.METHOD_CHANNEL_PACKAGE,
        ClazzConfig.Flutter.METHOD_CHANNEL_NAME
    ).copy(nullable = true)
    private val flutterEngineClassName = ClassName(
        ClazzConfig.Flutter.ENGINE_PACKAGE,
        ClazzConfig.Flutter.ENGINE_NAME
    ).copy(nullable = true)
    private val baseReceiverChannelClassName = ClassName(
        ClazzConfig.PACKAGE.BASE_NAME,
        ClazzConfig.BASE_RECEIVER_CHANNEL_NAME
    )

    private val resultType = ClazzConfig.Flutter.METHOD_RESULT_PACKAGE +
            "." +
            ClazzConfig.Flutter.METHOD_RESULT_NAME

    private var methodChannelName = ""
    private var clazzName = ""
    private var generateClazzName = ""
    private var channelName = ""
    private var isLackCreator = false

    fun handle(
        element: TypeElement,
        channelReceiverMap: HashMap<String, ClassName>,
        isLackCreator: Boolean = false
    ) {
        clazzName = element.simpleName.toString()
        generateClazzName = "${clazzName}Proxy"
        methodChannelName = element.simpleName.toString().decapitalize()
        this.isLackCreator = isLackCreator

        val channelAnnotation = element.getAnnotation(FlutterMethodChannel::class.java)
        val engineAnnotation = element.getAnnotation(FlutterEngine::class.java)
        channelName = channelAnnotation.channelName

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
        val flutterEngineProperty = PropertySpec.builder(
            "engine",
            flutterEngineClassName
        ).mutable()
            .addModifiers(KModifier.PRIVATE)
            .initializer("null")
            .build()
        propertyList.add(flutterEngineProperty)

        // private var channel: MethodChannel? = null
        val channelProperty = PropertySpec.builder(
            "channel",
            methodChannelClassName
        ).mutable()
            .addModifiers(KModifier.PRIVATE)
            .initializer("null")
            .build()
        propertyList.add(channelProperty)

        //  private val nezaMethodChannel:NezaMethodChannel = NezaMethodChannel()
        if (!isLackCreator) {
            val methodChannelProperty = PropertySpec
                .builder(
                    methodChannelName,
                    element.asType().asTypeName().copy(nullable = true)
                )
                .addModifiers(KModifier.PRIVATE)
                .mutable(true)
                .initializer("%T()", element)
                .build()
            propertyList.add(methodChannelProperty)
        }

        val getChannelFun = FunSpec.builder("getChannel")
            .addStatement("return channel")
            .build()
        funList.add(getChannelFun)

        val getChannelNameFun = FunSpec.builder("getChannelName")
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return name")
            .build()
        funList.add(getChannelNameFun)

        val getEngineIdFun = FunSpec.builder("getEngineId")
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return engineId")
            .build()
        funList.add(getEngineIdFun)

        val releaseFun = FunSpec.builder("release")
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("engine = null")
            .addStatement("channel?.setMethodCallHandler(null)")
            .addStatement("channel = null")
        if (!isLackCreator) {
            releaseFun.addStatement("$methodChannelName = null")
        }

        funList.add(releaseFun.build())

        val engineCreatorClazz = TypeSpec.classBuilder(generateClazzName)
            .addSuperinterface(baseReceiverChannelClassName)
            .addProperties(propertyList)
            .addInitializerBlock(assembleInit(element))
            .addFunctions(funList)
            .build()

        generatorClass(ClazzConfig.PACKAGE.CHANNEL_NAME, engineCreatorClazz)

        channelReceiverMap[channelName] = ClassName(
            ClazzConfig.PACKAGE.CHANNEL_NAME,
            generateClazzName
        )
    }

    private fun assembleInit(element: TypeElement): CodeBlock {
        val initBlock = CodeBlock.builder()
            .beginControlFlow(
                "engine = %T.getInstance().get(engineId)?.apply",
                engineCacheClassName
            )
            .addStatement("channel = MethodChannel(")
            .addStatement("  dartExecutor.binaryMessenger,")
            .addStatement("  name")
            .addStatement(")")
            .addStatement("channel?.setMethodCallHandler { call, result ->")

        initBlock.addStatement("  val method = call.method")
            .addStatement("  val arguments = call.arguments")
            .addStatement("  when (method) {")

        if (!isLackCreator) {
            // 拼装方法
            assembleMethod(methodChannelName, element, initBlock)
        }

        initBlock.addStatement("  }")
            .addStatement("}")
            .endControlFlow()

        return initBlock.build()
    }

    private fun assembleResultField(
        methodChannelName: String,
        element: TypeElement,
        initBlock: CodeBlock.Builder
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
            initBlock.addStatement("  $methodChannelName?.${resultElement.simpleName} = result")
        }
    }

    private fun assembleMethod(
        methodChannelName: String,
        element: TypeElement,
        initBlock: CodeBlock.Builder
    ) {
        val enclosedElements = element.enclosedElements

        val fieldElements = ArrayList<VariableElement>()
        for (item in enclosedElements) {
            if (item !is VariableElement) {
                continue
            }
            fieldElements.add(item)
        }

        val methodList = ArrayList<ExecutableElement>()
        for (item in enclosedElements) {
            if (item !is ExecutableElement) {
                continue
            }

            var isSkip = false
            for (fieldElement in fieldElements) {
                var fieldName = fieldElement.simpleName.toString()
                fieldName = fieldName.capitalize()
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
        }

        assembleMethod(
            methodChannelName = methodChannelName,
            element = element,
            methodList = methodList,
            initBlock = initBlock
        )
    }

    private fun assembleMethod(
        methodChannelName: String,
        element: Element,
        methodList: List<ExecutableElement>,
        initBlock: CodeBlock.Builder
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

            val receiveAnnotation = method.getAnnotation(Receive::class.java) ?: continue

            val onlyParamSize = getParamSize(parameters)
            val allParamSize = parameters.size
            val isNeedParseData = method.getAnnotation(ParseData::class.java) != null

            // 检测参数类型
            if (isNeedParseData) {
                parameters.forEach { parameter ->
                    // @Callback 类型检测
                    if (isCallback(parameter)) {
                        checkCallbackType(printer, resultType, parameter)
                        return@forEach
                    }

                    // 参数必须要为可空
                    val nullableAnnotation = parameter.getAnnotation(Nullable::class.java)
                    if (nullableAnnotation == null) {
                        printer.error(
                            "Parameter must be nullable " +
                                    "when you use @ParseData annotation. [$methodName]"
                        )
                        return
                    }
                }
            } else {
                // 非 @Callback 类型只能有一个
                if (onlyParamSize > 1) {
                    printer.error(
                        "There are more than one parameter in $methodName function." +
                                "You have two choice:\n" +
                                "1、Change the parameters size and the parameter must be a Any type\n" +
                                "2、User @ParseData annotation on this method, we'll parse the data " +
                                "for you，while it may be cause some error if you use the complex type."
                    )
                    return
                }
                parameters.forEach { parameter ->
                    // 检测 @Callback 类型参数
                    if (isCallback(parameter)) {
                        checkCallbackType(printer, resultType, parameter)
                        return@forEach
                    }
                    // 非 @Callback 类型必须为 Any
                    val type = TypeChangeUtils.change(parameter.asType().asTypeName().toString())
                    if (type != "Any") {
                        printer.error(
                            "Only Any type can be use in one parameter function. [$methodName]"
                        )
                        return
                    }
                }
            }

            val receiveName = if (receiveAnnotation.name.isEmpty()) {
                methodName
            } else {
                receiveAnnotation.name
            }

            // "sayHelloToNative" -> {
            val block = initBlock.addStatement("$spacing%S -> {", receiveName)
            if (allParamSize == 0) {
                block.addStatement("$spacing  $methodChannelName?.$methodName()")
            } else {
                block.addStatement("$spacing  $methodChannelName?.$methodName(")
                parameters.forEachIndexed { index, parameter ->
                    val paramName = parameter.simpleName
                    val paramType = parameter.asType().asTypeName()
                    val type = TypeChangeUtils.change(paramType.toString())
                    var statement = if (isCallback(parameter)) {
                        "$spacing    $paramName = result"
                    } else {
                        if (isNeedParseData) {
                            "$spacing    $paramName = call.argument<$type>(\"$paramName\")"
                        } else {
                            "$spacing    $paramName = arguments"
                        }
                    }
                    if (index < parameters.size - 1) {
                        statement += ","
                    }
                    block.addStatement(statement)
                }
                block.addStatement("$spacing  )")
            }

            block.addStatement("$spacing}")
            methodNameSet.add(methodName.toString())
        }
    }
}