package com.joyy.neza_compiler.processor.basicChannel

import com.joyy.neza_annotation.FlutterEngine
import com.joyy.neza_annotation.basic.FlutterBasicChannel
import com.joyy.neza_annotation.common.Callback
import com.joyy.neza_annotation.method.HandleMessage
import com.joyy.neza_compiler.Printer
import com.joyy.neza_compiler.config.ClazzConfig
import com.joyy.neza_compiler.utils.EngineHelper
import com.joyy.neza_compiler.utils.TypeChangeUtils
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
import java.util.Locale
import javax.annotation.processing.Filer
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/12 11:39 上午
 * @email: 56002982@qq.com
 * @des: 接收者处理器
 */
class ReceiverProcessor(
    private val filer: Filer,
    private val printer: Printer,
    private val typeUtils: Types
) {

    fun handle(
        roundEnv: RoundEnvironment,
        element: TypeElement,
        channelReceiverMap: HashMap<String, ChannelInfo>,
        isLackCreator: Boolean = false
    ) {
        val clazzName = element.simpleName
        val generateClazzName = "${clazzName}Proxy"
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

        checkCodecClass(codecTypeMirror = codecTypeMirror)
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
        val engineClassName = ClassName(
            ClazzConfig.Flutter.ENGINE_PACKAGE,
            ClazzConfig.Flutter.ENGINE_NAME
        )
        val flutterEngineProperty = PropertySpec.builder(
            "engine",
            engineClassName.copy(nullable = true)
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
        val basicChannelName = element.simpleName.toString().capitalize()
        if (!isLackCreator) {
            val basicChannelProperty = PropertySpec.builder(
                basicChannelName,
                element.asType().asTypeName()
            ).addModifiers(KModifier.PRIVATE)
                .initializer("%T()", element)
                .build()
            propertyList.add(basicChannelProperty)
        }

        val contextClassName = ClassName(
            ClazzConfig.Android.CONTEXT_PACKAGE,
            ClazzConfig.Android.CONTEXT_NAME
        )
        val engineHelperClassName = ClassName(
            ClazzConfig.ENGINE_HELPER_PACKAGE,
            ClazzConfig.ENGINE_HELPER_NAME
        )
        val initFun = FunSpec.builder("init")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("context", contextClassName)
            .beginControlFlow(
                "engine = %T.getFlutterEngine(engineId)?.apply",
                engineHelperClassName
            )
            .addStatement("channel = BasicMessageChannel(")
            .addStatement("  dartExecutor.binaryMessenger,")
            .addStatement("  name,")
            .addStatement("  %T.INSTANCE", codecTypeMirror)
            .addStatement(")")
            .addStatement("channel?.setMessageHandler { message, reply ->")

        if (!isLackCreator) {
            assembleReplyField(
                basicChannelName,
                element,
                initFun,
                genericsTypeName
            )
            // 拼装方法
            assembleHandleMethod(element, initFun)
        }

        initFun.addStatement("}")
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
            ClazzConfig.Channel.BASIC_CHANNEL_NAME
        ).parameterizedBy(
            TypeChangeUtils.change(printer, genericsType)
        )
        val receiverClazz = TypeSpec.classBuilder(generateClazzName)
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

        FileSpec.get(ClazzConfig.PACKAGE.NEZA_CHANNEL, receiverClazz)
            .writeTo(filer)

        channelReceiverMap[channelName] = ChannelInfo(
            ClassName(
                ClazzConfig.PACKAGE.NEZA_CHANNEL,
                generateClazzName
            ),
            genericsType
        )
    }

    private fun checkCodecClass(
        codecTypeMirror: TypeMirror
    ) {
        if (codecTypeMirror !is DeclaredType) {
            printer.error("$codecTypeMirror must be a class.")
            return
        }
        val codecElement = codecTypeMirror.asElement()
        if (codecElement !is TypeElement) {
            printer.error("$codecTypeMirror must be a class.")
            return
        }

        val enclosedElements = codecElement.enclosedElements
        var isFindInstance = false
        for (enclosedElement in enclosedElements) {
            if (enclosedElement.kind != ElementKind.FIELD) {
                continue
            }
            if (enclosedElement !is VariableElement) {
                continue
            }

            if (enclosedElement.simpleName.toString() != "INSTANCE") {
                continue
            }

            var isFindStatic = false
            for (modifier in enclosedElement.modifiers) {
                if (modifier == Modifier.STATIC) {
                    isFindStatic = true
                }
            }

            if (isFindStatic) {
                isFindInstance = true
                break
            }
        }

        if (!isFindInstance) {
            printer.error("You need support a static INSTANCE field. [$codecElement]")
            return
        }
    }

    private fun assembleReplyField(
        basicChannelName: String,
        element: TypeElement,
        initFun: FunSpec.Builder,
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
            initFun.addStatement("  $basicChannelName.${resultElement.simpleName} = reply")
        }
    }

    private fun assembleHandleMethod(
        typeElement: TypeElement,
        initFun: FunSpec.Builder
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
                initFun = initFun,
                handleMethod = handleMethod
            )
        }
    }

    private fun assembleHandleMethod(
        typeElement: TypeElement,
        initFun: FunSpec.Builder,
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
            initFun.addStatement("$spacing  %T.$methodName()", typeElement)
        } else {    // 构建参数
            initFun.addStatement("$spacing  %T.$methodName(", typeElement)
            val parameter = parameters[0]
            val paramName = parameter.simpleName
            parameter.getAnnotation(Nullable::class.java)
                ?: error("Parameter must be nullable.[$methodName]")

            initFun.addStatement(
                "$spacing    $paramName = message"
            )

            initFun.addStatement("$spacing  )")
        }
    }
}