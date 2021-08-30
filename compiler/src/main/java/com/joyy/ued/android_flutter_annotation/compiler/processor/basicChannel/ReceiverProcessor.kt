package com.joyy.ued.android_flutter_annotation.compiler.processor.basicChannel

import com.joyy.ued.android_flutter_annotation.annotation.FlutterEngine
import com.joyy.ued.android_flutter_annotation.annotation.basic.FlutterBasicChannel
import com.joyy.ued.android_flutter_annotation.annotation.common.Callback
import com.joyy.ued.android_flutter_annotation.annotation.common.Receive
import com.joyy.ued.android_flutter_annotation.annotation.method.ParseData
import com.joyy.ued.android_flutter_annotation.compiler.Printer
import com.joyy.ued.android_flutter_annotation.compiler.base.BaseProcessor
import com.joyy.ued.android_flutter_annotation.compiler.config.ClazzConfig
import com.joyy.ued.android_flutter_annotation.compiler.utils.EngineHelper
import com.joyy.ued.android_flutter_annotation.compiler.utils.ReceiverHelper
import com.joyy.ued.android_flutter_annotation.compiler.utils.TypeChangeUtils
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.annotations.Nullable
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/12 11:39 上午
 * @email: 56002982@qq.com
 * @des: Basic Message Channel 接收者
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
    private val engineClassName = ClassName(
        ClazzConfig.Flutter.ENGINE_PACKAGE,
        ClazzConfig.Flutter.ENGINE_NAME
    ).copy(nullable = true)
    private val baseReceiverChannelClassName = ClassName(
        ClazzConfig.PACKAGE.BASE_NAME,
        ClazzConfig.BASE_RECEIVER_CHANNEL_NAME
    )

    private var isLackCreator = false
    private var clazzName = ""
    private var generateClazzName = ""
    private var basicChannelName = ""

    fun handle(
        element: TypeElement,
        channelReceiverMap: HashMap<String, ChannelInfo>,
        isLackCreator: Boolean = false
    ) {
        clazzName = element.simpleName.toString()
        generateClazzName = "${clazzName}Proxy"
        this.isLackCreator = isLackCreator
        basicChannelName = element.simpleName.toString().decapitalize()

        val channelAnnotation = element.getAnnotation(FlutterBasicChannel::class.java)
        val engineAnnotation = element.getAnnotation(FlutterEngine::class.java)
        val channelName = channelAnnotation.channelName

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

        BasicProcessorUtils.checkCodecClass(codecTypeMirror, printer)
        val genericsTypeName = TypeChangeUtils.change(printer, genericsType)

        val propertyList = ArrayList<PropertySpec>()
        val funList = ArrayList<FunSpec>()

        // private val engineId = "NEZA_ENGINE_ID"
        val engineId = engineAnnotation?.engineId ?: EngineHelper.getEngineId(roundEnv)
        val engineIdProperty = PropertySpec.builder("engineId", String::class)
            .addModifiers(KModifier.PRIVATE)
            .initializer("%S", engineId)
            .build()
        propertyList.add(engineIdProperty)

        // private val name = "com.zinc.android_flutter_annotation/nezaBasicChannel"
        val nameProperty = PropertySpec.builder("name", String::class)
            .addModifiers(KModifier.PRIVATE)
            .initializer("%S", channelName)
            .build()
        propertyList.add(nameProperty)

        // private var engine: FlutterEngine? = null
        val flutterEngineProperty = PropertySpec.builder(
            "engine",
            engineClassName
        ).mutable()
            .addModifiers(KModifier.PRIVATE)
            .initializer("null")
            .build()
        propertyList.add(flutterEngineProperty)

        // private var channel: BasicMessageChannel? = null
        val channelClassName = ClassName(
            ClazzConfig.Flutter.METHOD_CHANNEL_PACKAGE,
            ClazzConfig.Flutter.BASIC_CHANNEL_NAME
        ).parameterizedBy(
            genericsTypeName
        )
        val channelProperty = PropertySpec.builder(
            "channel",
            channelClassName.copy(nullable = true)
        ).mutable()
            .addModifiers(KModifier.PRIVATE)
            .initializer("null")
            .build()
        propertyList.add(channelProperty)

        // private val nezaStringBasicChannel: NezaStringBasicChannel = NezaStringBasicChannel()

        if (!isLackCreator) {
            val basicChannelProperty = PropertySpec
                .builder(
                    basicChannelName,
                    element.asType().asTypeName().copy(nullable = true)
                )
                .mutable(true)
                .addModifiers(KModifier.PRIVATE)
                .initializer("%T()", element)
                .build()
            propertyList.add(basicChannelProperty)
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
            .addStatement("channel?.setMessageHandler(null)")
            .addStatement("channel = null")
        if (!isLackCreator) {
            releaseFun.addStatement("$basicChannelName = null")
        }
        funList.add(releaseFun.build())

        // MethodChannelInterface
        val receiverClazz = TypeSpec.classBuilder(generateClazzName)
            .addSuperinterface(baseReceiverChannelClassName)
            .addProperties(propertyList)
            .addInitializerBlock(
                assembleInit(
                    element,
                    codecTypeMirror,
                    genericsTypeName
                )
            )
            .addFunctions(funList)
            .build()

        generatorClass(ClazzConfig.PACKAGE.CHANNEL_NAME, receiverClazz)

        channelReceiverMap[channelName] = ChannelInfo(
            ClassName(
                ClazzConfig.PACKAGE.CHANNEL_NAME,
                generateClazzName
            ),
            genericsType
        )
    }

    private fun assembleInit(
        element: TypeElement,
        codecTypeMirror: TypeMirror,
        genericsTypeName: TypeName
    ): CodeBlock {
        val initBlock = CodeBlock.builder()
            .beginControlFlow(
                "engine = %T.getInstance().get(engineId)?.apply",
                engineCacheClassName
            )
            .addStatement("channel = BasicMessageChannel(")
            .addStatement("  dartExecutor.binaryMessenger,")
            .addStatement("  name,")
            .addStatement("  %T.INSTANCE", codecTypeMirror)
            .addStatement(")")
            .addStatement("channel?.setMessageHandler { message, reply ->")

        if (!isLackCreator) {
            // 拼装方法
            assembleHandleMethod(
                typeElement = element,
                initBlock = initBlock,
                genericsTypeName = genericsTypeName
            )
        }

        initBlock.addStatement("}")
            .endControlFlow()
        return initBlock.build()
    }

    private fun assembleHandleMethod(
        typeElement: TypeElement,
        initBlock: CodeBlock.Builder,
        genericsTypeName: TypeName
    ) {
        val enclosedElements = typeElement.enclosedElements
        var handleMethod: ExecutableElement? = null
        for (enclosedElement in enclosedElements) {
            if (enclosedElement !is ExecutableElement) {
                continue
            }
            if (enclosedElement.getAnnotation(Receive::class.java) == null) {
                continue
            }
            handleMethod = enclosedElement
        }
        if (handleMethod != null) {
            assembleHandleMethod(
                initBlock = initBlock,
                typeElement = typeElement,
                handleMethod = handleMethod,
                genericsTypeName = genericsTypeName
            )
        }
    }

    private fun assembleHandleMethod(
        typeElement: TypeElement,
        initBlock: CodeBlock.Builder,
        genericsTypeName: TypeName,
        handleMethod: ExecutableElement
    ) {
        val spacing = "  "

        val methodName = handleMethod.simpleName
        val parameters = handleMethod.parameters
        val onlyParamSize = ReceiverHelper.getParamSize(parameters)
        val allParamSize = parameters.size

        val className = ClassName(
            ClazzConfig.Flutter.BASIC_REPLY_PACKAGE,
            ClazzConfig.Flutter.BASIC_REPLY_NAME
        ).parameterizedBy(
            genericsTypeName
        )
        val replyType = ClazzConfig.Flutter.BASIC_REPLY_PACKAGE +
                "." +
                ClazzConfig.Flutter.BASIC_REPLY_NAME

        // 检测参数类型
        // 非 @Callback 类型只能有一个
        if (onlyParamSize > 1) {
            printer.error("Basic message channel only can accept one parameters.")
            return
        }
        parameters.forEach { parameter ->
            // 检测 @Callback 类型参数
            if (ReceiverHelper.isCallback(parameter)) {
                val type = parameter.asType().toString()
                if (type == className.toString()) {
                    printer.error(
                        "The parameter must be a $replyType type if you use @Callback." +
                                "[$typeElement -- $parameter]"
                    )
                    return
                }
                return@forEach
            }
            if (parameter.getAnnotation(Nullable::class.java) == null) {
                printer.error("Parameter must be nullable.[$methodName]")
            }
            // 非 @Callback 类型必须为
            val type = parameter.asType().toString()
            if (type == className.toString()) {
                printer.error(
                    "The parameter must be a $replyType type if you use @Callback." +
                            "[$typeElement -- $parameter]"
                )
                return
            }
        }

        if (allParamSize == 0) {   // 没有参数
            initBlock.addStatement("$spacing$basicChannelName?.$methodName()")
        } else {
            initBlock.addStatement("$spacing$basicChannelName?.$methodName(")
            parameters.forEachIndexed { index, parameter ->
                val paramName = parameter.simpleName

                var statement = if (ReceiverHelper.isCallback(parameter)) {
                    "$spacing  $paramName = reply"
                } else {
                    "$spacing  $paramName = message"
                }

                if (index < parameters.size - 1) {
                    statement += ","
                }

                initBlock.addStatement(statement)
            }
            initBlock.addStatement("$spacing)")
        }
    }
}