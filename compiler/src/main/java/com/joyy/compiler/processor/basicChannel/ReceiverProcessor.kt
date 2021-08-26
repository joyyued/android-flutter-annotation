package com.joyy.compiler.processor.basicChannel

import com.joyy.annotation.FlutterEngine
import com.joyy.annotation.basic.FlutterBasicChannel
import com.joyy.annotation.common.Callback
import com.joyy.annotation.method.HandleMessage
import com.joyy.compiler.Printer
import com.joyy.compiler.base.BaseProcessor
import com.joyy.compiler.config.ClazzConfig
import com.joyy.compiler.utils.EngineHelper
import com.joyy.compiler.utils.TypeChangeUtils
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
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
            assembleReplyField(
                element,
                initBlock,
                genericsTypeName
            )
            // 拼装方法
            assembleHandleMethod(element, initBlock)
        }

        initBlock.addStatement("}")
            .endControlFlow()
        return initBlock.build()
    }

    private fun assembleReplyField(
        element: TypeElement,
        initBlock: CodeBlock.Builder,
        genericsTypeName: TypeName
    ) {
        val enclosedElements = element.enclosedElements
        val resultPath = ClazzConfig.Flutter.BASIC_REPLY_PACKAGE +
                "." +
                ClazzConfig.Flutter.BASIC_REPLY_NAME

        val className = ClassName(
            ClazzConfig.Flutter.BASIC_REPLY_PACKAGE,
            ClazzConfig.Flutter.BASIC_REPLY_NAME
        ).parameterizedBy(
            genericsTypeName
        )

        val resultElements = ArrayList<VariableElement>()
        for (item in enclosedElements) {
            if (item !is VariableElement) {
                continue
            }

            if (item.getAnnotation(Callback::class.java) == null) {
                continue
            }

            val type = item.asType().toString()
            if (type == className.toString()) {
                printer.error(
                    "The parameter must be a $resultPath type if you use @Callback." +
                            "[$element -- $item]"
                )
                return
            }
            resultElements.add(item)
        }

        for (resultElement in resultElements) {
            initBlock.addStatement("  $basicChannelName?.${resultElement.simpleName} = reply")
        }
    }

    private fun assembleHandleMethod(
        typeElement: TypeElement,
        initBlock: CodeBlock.Builder
    ) {
        val enclosedElements = typeElement.enclosedElements
        var handleMethod: ExecutableElement? = null
        for (enclosedElement in enclosedElements) {
            if (enclosedElement !is ExecutableElement) {
                continue
            }
            if (enclosedElement.getAnnotation(HandleMessage::class.java) == null) {
                continue
            }
            handleMethod = enclosedElement
        }
        if (handleMethod != null) {
            assembleHandleMethod(
                typeElement = typeElement,
                initBlock = initBlock,
                handleMethod = handleMethod
            )
        }
    }

    private fun assembleHandleMethod(
        typeElement: TypeElement,
        initBlock: CodeBlock.Builder,
        handleMethod: ExecutableElement
    ) {
        val spacing = "  "

        val methodName = handleMethod.simpleName
        val parameters = handleMethod.parameters

        if (parameters.size > 1) {
            printer.error(
                "Basic method channel only can accept one parameters."
            )
            return
        }

        if (parameters.isEmpty()) {   // 没有参数
            initBlock.addStatement("$spacing$basicChannelName?.$methodName()")
        } else {    // 构建参数
            initBlock.addStatement("$spacing$basicChannelName?.$methodName(")
            val parameter = parameters[0]
            val paramName = parameter.simpleName
            parameter.getAnnotation(Nullable::class.java)
                ?: error("Parameter must be nullable.[$methodName]")

            initBlock.addStatement(
                "$spacing  $paramName = message"
            )

            initBlock.addStatement("$spacing)")
        }
    }
}