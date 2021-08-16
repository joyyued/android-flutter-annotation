package com.joyy.neza_compiler.processor.basicChannel

import com.joyy.neza_annotation.FlutterEngine
import com.joyy.neza_annotation.basic.FlutterBasicChannel
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
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
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
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypeException
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
        element: Element,
        channelReceiverMap: HashMap<String, ChannelInfo>
    ) {
        val clazzName = element.simpleName
        val generateClazzName = "${clazzName}Proxy"
        val channelAnnotation = element.getAnnotation(FlutterBasicChannel::class.java)
        val engineAnnotation = element.getAnnotation(FlutterEngine::class.java)
        val channelName = channelAnnotation.channelName

        var codecTypeMirror: TypeMirror? = null
        try {
            channelAnnotation.codecClass
        } catch (e: MirroredTypeException) {
            codecTypeMirror = e.typeMirror
        }
        if (codecTypeMirror == null) {
            printer.error("Can not get codec.Check the codec is exist.")
            return
        }

        val genericsType = getGenericsType(codecTypeMirror)
        if (genericsType == null) {
            printer.error("You must support a generic in codec.")
            return
        }

        // private val engineId = "NEZA_ENGINE_ID"
        val engineId = engineAnnotation?.engineId ?: EngineHelper.getEngineId(roundEnv)
        val engineIdProperty = PropertySpec.builder("engineId", String::class)
            .addModifiers(KModifier.PRIVATE)
            .initializer("%S", engineId)
            .build()

        // private val name = "com.zinc.android_flutter_annotation/nezaBasicChannel"
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

        // private var channel: BasicMessageChannel? = null
        val channelClassName = ClassName(
            ClazzConfig.Flutter.METHOD_CHANNEL_PACKAGE,
            ClazzConfig.Flutter.Basic_CHANNEL_NAME,
        ).parameterizedBy(
            TypeChangeUtils.change(genericsType.asTypeName())
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
            .addStatement("channel = BasicMessageChannel(")
            .addStatement("  dartExecutor.binaryMessenger,")
            .addStatement("  name,")
            .addStatement("  %T.INSTANCE", codecTypeMirror)
            .addStatement(")")
            .addStatement("channel?.setMessageHandler { message, reply ->")

        // 拼装方法
        assembleMethod(element, initFun)

        initFun.addStatement("}")
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
            ClazzConfig.Channel.BASIC_CHANNEL_NAME,
        ).parameterizedBy(
            TypeChangeUtils.change(genericsType.asTypeName())
        )
        val receiverClazz = TypeSpec.classBuilder(generateClazzName)
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

    private fun assembleMethod(element: Element, initFun: FunSpec.Builder) {
        val spacing = "  "
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
                printer.error(
                    "Method name[$methodName] already exists in $element." +
                            "You should use different method name in this class."
                )
            } else if (parameters.size > 1) {
                printer.error(
                    "Basic method channel only can accept one parameters."
                )
            }

            if (parameters.isEmpty()) {   // 没有参数
                initFun.addStatement("$spacing  %T.$methodName()", element)
            } else {    // 构建参数
                initFun.addStatement("$spacing  %T.$methodName(", element)
                for (parameter in parameters) {
                    parameter ?: continue

                    val paramName = parameter.simpleName
                    val paramType = parameter.asType()
                    val nullableAnnotation = parameter.getAnnotation(Nullable::class.java)

                    val type = TypeChangeUtils.change(paramType.toString())

                    if (nullableAnnotation == null) {
                        error("Parameter must be nullable.[$methodName]")
                    }
                    initFun.addStatement(
                        "$spacing    $paramName = message",
                    )
                }
                initFun.addStatement("$spacing  )")
            }
            methodNameSet.add(methodName.toString())
        }
    }

    private fun getGenericsType(codecTypeMirror: TypeMirror): TypeMirror? {
        if (codecTypeMirror !is DeclaredType) {
            printer.error("$codecTypeMirror must be a class.")
            return null
        }
        val codecElement = codecTypeMirror.asElement()
        if (codecElement !is TypeElement) {
            printer.error("$codecTypeMirror must be a class.")
            return null
        }
        printer.note(
            "[test] typeElement: $codecElement |" +
                    " ${codecElement.kind} |" +
                    " ${codecElement.superclass} |" +
                    " ${codecElement.interfaces} |" +
                    " ${codecElement.nestingKind} |" +
                    " ${codecElement.enclosedElements} |" +
                    " ${codecElement.annotationMirrors} |" +
                    " ${codecElement.kind}"
        )

        var extendTypeMirror: TypeMirror? = null
        for (interfaceTypeMirror in codecElement.interfaces) {
            if (!interfaceTypeMirror.toString().startsWith(
                    "io.flutter.plugin.common.MessageCodec"
                )
            ) {
                continue
            }
            if (interfaceTypeMirror !is DeclaredType) {
                continue
            }
            printer.note(
                "[test] interfaceTypeMirror: $interfaceTypeMirror |" +
                        " ${interfaceTypeMirror.annotationMirrors} |" +
                        " ${interfaceTypeMirror.enclosingType} |" +
                        " ${interfaceTypeMirror.typeArguments} |" +
                        " ${interfaceTypeMirror.kind}"
            )
            val typeArguments = interfaceTypeMirror.typeArguments
            if (typeArguments.size <= 0) {
                continue
            }
            extendTypeMirror = typeArguments[0]
        }

        return extendTypeMirror
    }
}