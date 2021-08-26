package com.joyy.compiler.processor.manager

import com.joyy.annotation.FlutterEngine
import com.joyy.annotation.basic.FlutterBasicChannel
import com.joyy.annotation.event.FlutterEventChannel
import com.joyy.annotation.method.FlutterMethodChannel
import com.joyy.annotation.model.ChannelType
import com.joyy.compiler.Printer
import com.joyy.compiler.base.BaseProcessor
import com.joyy.compiler.config.ClazzConfig
import com.joyy.compiler.utils.EngineHelper
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/11 10:04 上午
 * @email: 56002982@qq.com
 * @des: flutter manager 处理器
 */
class FlutterManagerProcessor(
    printer: Printer,
    processingEnv: ProcessingEnvironment,
    roundEnv: RoundEnvironment
) : BaseProcessor(
    printer,
    processingEnv,
    roundEnv
) {

    private val methodReceiverClassNameMap = HashMap<String, String>()
    private val methodSenderClassNameMap = HashMap<String, String>()

    private val eventChannelMap = HashMap<String, String>()

    private val basicReceiverClassNameMap = HashMap<String, String>()
    private val basicSenderClassNameMap = HashMap<String, String>()

    private val senderType = HashMap::class.asTypeName().parameterizedBy(
        String::class.asTypeName(),
        ClassName(
            ClazzConfig.PACKAGE.BASE_NAME,
            ClazzConfig.BASE_SENDER_CHANNEL_NAME
        )
    )
    private val receiverType = HashMap::class.asTypeName().parameterizedBy(
        String::class.asTypeName(),
        ClassName(
            ClazzConfig.PACKAGE.BASE_NAME,
            ClazzConfig.BASE_RECEIVER_CHANNEL_NAME
        )
    )

    fun process() {
        printer.note("Flutter Manager Processor running.")

        createFlutterChannel()

        val engineCreatorClazz = TypeSpec.objectBuilder(ClazzConfig.FLUTTER_MANAGER_NAME)
            .addProperty(createAllEngineProperty())
            .addProperties(createChannelMap())
            .addProperty(
                PropertySpec
                    .builder(
                        "channels",
                        ClassName(
                            ClazzConfig.PACKAGE.ENGINE_NAME,
                            ClazzConfig.FLUTTER_CHANNEL_NAME
                        ).copy(nullable = true)
                    )
                    .mutable(true)
                    .initializer("null")
                    .build()
            )
            .addType(createEngineObject())
            .addFunction(initFunction())
            .addFunction(releaseFunction())
            .build()

        generatorClass(ClazzConfig.PACKAGE.ENGINE_NAME, engineCreatorClazz)
    }

    private fun createFlutterChannel() {
        val methodChannelPropertySpecList = handleMethodChannel(roundEnv)
        val eventChannelPropertySpecList = handleEventChannel(roundEnv)
        val basicChannelChannelPropertySpecList = handleBasicChannel(roundEnv)

        val flutterChannelType = TypeSpec.classBuilder(ClazzConfig.FLUTTER_CHANNEL_NAME)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        "senderChannelMap",
                        senderType
                    )
                    .addParameter(
                        "receiverChannelMap",
                        receiverType
                    )
                    .build()
            )
            .addProperty(
                PropertySpec
                    .builder(
                        "senderChannelMap",
                        senderType,
                        KModifier.PRIVATE
                    )
                    .initializer("senderChannelMap")
                    .build()
            )
            .addProperty(
                PropertySpec
                    .builder(
                        "receiverChannelMap",
                        receiverType,
                        KModifier.PRIVATE
                    )
                    .initializer("receiverChannelMap")
                    .build()
            )
            .addProperties(methodChannelPropertySpecList)
            .addProperties(eventChannelPropertySpecList)
            .addProperties(basicChannelChannelPropertySpecList)
            .build()
        generatorClass(ClazzConfig.PACKAGE.ENGINE_NAME, flutterChannelType)
    }

    private fun createEngineObject(): TypeSpec {
        return TypeSpec.objectBuilder(ClazzConfig.FLUTTER_ENGINE_NAME)
            .addProperty(
                PropertySpec
                    .builder("DEFAULT_ENGINE", String::class)
                    .initializer("%S", EngineHelper.getEngineId(roundEnv))
                    .addModifiers(KModifier.CONST)
                    .build()
            )
            .addFunction(
                FunSpec
                    .builder("getEngine")
                    .addParameter(
                        ParameterSpec
                            .builder("engineId", String::class)
                            .defaultValue("DEFAULT_ENGINE")
                            .build()
                    )
                    .returns(
                        ClassName(
                            ClazzConfig.Flutter.ENGINE_PACKAGE,
                            ClazzConfig.Flutter.ENGINE_NAME
                        ).copy(nullable = true)
                    )
                    .addStatement(
                        "return %T.getInstance().get(engineId)",
                        ClassName(
                            ClazzConfig.Flutter.ENGINE_PACKAGE,
                            ClazzConfig.Flutter.ENGINE_CACHE_NAME
                        )
                    )
                    .build()
            )
            .build()
    }

    private fun createAllEngineProperty(): PropertySpec {
        val element = EngineHelper.getFlutterEngineElements(roundEnv).first()
        val annotation = element.getAnnotation(FlutterEngine::class.java)
        val engineId = annotation.engineId

        val arrayListClassName = ClassName(
            "kotlin.collections",
            "ArrayList"
        ).parameterizedBy(String::class.asTypeName())

        return PropertySpec
            .builder(
                "engineIds",
                arrayListClassName
            ).initializer(
                "arrayListOf(%S)",
                engineId
            )
            .addModifiers(KModifier.PRIVATE)
            .build()
    }

    private fun createChannelMap(): List<PropertySpec> {
        val list = ArrayList<PropertySpec>()

        val senderProperty = PropertySpec
            .builder(
                "senderChannelMap",
                senderType
            )
            .initializer("%T()", senderType)
            .addModifiers(KModifier.PRIVATE)
            .build()
        list.add(senderProperty)

        val receiverProperty = PropertySpec
            .builder(
                "receiverChannelMap",
                receiverType
            )
            .initializer("%T()", receiverType)
            .addModifiers(KModifier.PRIVATE)
            .build()
        list.add(receiverProperty)

        return list
    }

    private fun handleMethodChannel(roundEnv: RoundEnvironment): ArrayList<PropertySpec> {
        val methodChannel = roundEnv.getElementsAnnotatedWith(FlutterMethodChannel::class.java)

        val methodReceiverChannelNameSet = HashSet<String>()
        val methodSenderChannelNameSet = HashSet<String>()

        for (element in methodChannel) {
            if (element !is TypeElement) {
                continue
            }

            val clazzName = element.simpleName.toString()
            val annotation = element.getAnnotation(FlutterMethodChannel::class.java) ?: continue
            if (annotation.type == ChannelType.RECEIVER) {
                methodReceiverClassNameMap[clazzName] = annotation.channelName
                methodReceiverChannelNameSet.add(annotation.channelName)
            } else {
                methodSenderClassNameMap[clazzName] = annotation.channelName
                methodSenderChannelNameSet.add(annotation.channelName)
            }
        }

        val needCreatorChannelName: HashSet<String> = HashSet()
        for (channel in methodSenderChannelNameSet) {
            if (methodReceiverChannelNameSet.contains(channel)) {
                continue
            }
            needCreatorChannelName.add(channel)
        }
        val needCreatorChannelElement: HashSet<TypeElement> = HashSet()
        for (element in methodChannel) {
            if (element !is TypeElement) {
                continue
            }
            val annotation = element.getAnnotation(FlutterMethodChannel::class.java) ?: continue

            if (needCreatorChannelName.contains(annotation.channelName)) {
                needCreatorChannelElement.add(element)
                methodReceiverClassNameMap[element.simpleName.toString()] = annotation.channelName
            }
        }
        val methodProcessor = com.joyy.compiler.processor.methodChannel.ReceiverProcessor(
            printer = printer,
            processingEnv = processingEnv,
            roundEnv = roundEnv
        )
        for (typeElement in needCreatorChannelElement) {
            methodProcessor.handle(
                element = typeElement,
                channelReceiverMap = HashMap(),
                isLackCreator = true
            )
        }

        return assembleProperty(
            methodSenderClassNameMap,
            "return senderChannelMap[%S] as? %T",
            "============ Method Channel ============"
        )
    }

    private fun handleEventChannel(roundEnv: RoundEnvironment): ArrayList<PropertySpec> {
        val eventChannel = roundEnv.getElementsAnnotatedWith(FlutterEventChannel::class.java)

        for (element in eventChannel) {
            if (element !is TypeElement) {
                continue
            }
            val annotation = element.getAnnotation(FlutterEventChannel::class.java) ?: continue
            eventChannelMap[element.simpleName.toString()] = annotation.channelName
        }

        return assembleProperty(
            eventChannelMap,
            "return receiverChannelMap[%S] as? %T",
            "============ Event Channel ============"
        )
    }

    private fun handleBasicChannel(roundEnv: RoundEnvironment): ArrayList<PropertySpec> {
        val basicChannel = roundEnv.getElementsAnnotatedWith(FlutterBasicChannel::class.java)

        val basicReceiverChannelNameSet = HashSet<String>()
        val basicSenderChannelNameSet = HashSet<String>()

        for (element in basicChannel) {
            if (element !is TypeElement) {
                continue
            }

            val clazzName = element.simpleName.toString()
            val annotation = element.getAnnotation(FlutterBasicChannel::class.java) ?: continue
            if (annotation.type == ChannelType.RECEIVER) {
                basicReceiverClassNameMap[clazzName] = annotation.channelName
                basicReceiverChannelNameSet.add(annotation.channelName)
            } else {
                basicSenderClassNameMap[clazzName] = annotation.channelName
                basicSenderChannelNameSet.add(annotation.channelName)
            }
        }

        val needCreatorChannelName: HashSet<String> = HashSet()
        for (channel in basicSenderChannelNameSet) {
            if (basicReceiverChannelNameSet.contains(channel)) {
                continue
            }
            needCreatorChannelName.add(channel)
        }
        val needCreatorChannelElement: HashSet<TypeElement> = HashSet()
        for (element in basicChannel) {
            if (element !is TypeElement) {
                continue
            }
            val annotation = element.getAnnotation(FlutterBasicChannel::class.java) ?: continue

            if (needCreatorChannelName.contains(annotation.channelName)) {
                needCreatorChannelElement.add(element)
                basicReceiverClassNameMap[element.simpleName.toString()] = annotation.channelName
            }
        }
        val basicProcessor = com.joyy.compiler.processor.basicChannel.ReceiverProcessor(
            processingEnv = processingEnv,
            printer = printer,
            roundEnv = roundEnv
        )
        for (typeElement in needCreatorChannelElement) {
            basicProcessor.handle(
                element = typeElement,
                channelReceiverMap = HashMap(),
                isLackCreator = true
            )
        }

        return assembleProperty(
            basicSenderClassNameMap,
            "return senderChannelMap[%S] as? %T",
            "============ Basic Channel ============"
        )
    }

    private fun assembleProperty(
        map: HashMap<String, String>,
        initializer: String,
        doc: String
    ): ArrayList<PropertySpec> {
        val propertySpecList = ArrayList<PropertySpec>()
        var isFirst = true
        for ((element, channelName) in map) {
            val className = ClassName(
                ClazzConfig.PACKAGE.CHANNEL_NAME,
                "${element}Impl"
            )
            val proName = element.decapitalize()

            val propertySpec = PropertySpec
                .builder(
                    proName,
                    className.copy(nullable = true)
                )
                .getter(
                    FunSpec.getterBuilder()
                        .addStatement(initializer, channelName, className)
                        .build()
                )

            if (isFirst) {
                isFirst = false
                propertySpec.addKdoc(doc)
            }

            propertySpecList.add(propertySpec.build())
        }
        return propertySpecList
    }

    private fun initFunction(): FunSpec {
        val initFun = FunSpec.builder("init")
            .addParameter(
                "context",
                ClassName(
                    ClazzConfig.Android.CONTEXT_PACKAGE,
                    ClazzConfig.Android.CONTEXT_NAME
                )
            )
            .addStatement(
                "%T.createEngine(context, engineIds)",
                ClassName(
                    ClazzConfig.PACKAGE.ENGINE_NAME,
                    ClazzConfig.ENGINE_UTILS_NAME
                )
            )
            .addStatement("")

        for ((element, channelName) in methodReceiverClassNameMap) {
            val proxyClazzName = "${element}Proxy"
            val implClazzName = "${element}Impl"
            val objectName = proxyClazzName.decapitalize()

            initFun
                .addStatement(
                    "val $objectName = %T()",
                    ClassName(
                        ClazzConfig.PACKAGE.CHANNEL_NAME,
                        proxyClazzName
                    )
                )
                .addStatement("receiverChannelMap[%S] = $objectName", channelName)

            if (methodSenderClassNameMap.containsValue(channelName)) {
                initFun.addStatement(
                    "senderChannelMap[%S] = %T($objectName.getChannel())",
                    channelName,
                    ClassName(
                        ClazzConfig.PACKAGE.CHANNEL_NAME,
                        implClazzName
                    )
                )
            }

            initFun.addStatement("")
        }

        for ((element, channelName) in eventChannelMap) {
            val implClazzName = "${element}Impl"
            val objectName = implClazzName.decapitalize()
            initFun
                .addStatement(
                    "val $objectName = %T()",
                    ClassName(
                        ClazzConfig.PACKAGE.CHANNEL_NAME,
                        implClazzName
                    )
                )
                .addStatement(
                    "receiverChannelMap[%S] = $objectName",
                    channelName
                )
                .addStatement("")
        }

        for ((element, channelName) in basicReceiverClassNameMap) {
            val proxyClazzName = "${element}Proxy"
            val implClazzName = "${element}Impl"
            val objectName = proxyClazzName.decapitalize()

            initFun
                .addStatement(
                    "val $objectName = %T()",
                    ClassName(
                        ClazzConfig.PACKAGE.CHANNEL_NAME,
                        proxyClazzName
                    )
                )
                .addStatement("receiverChannelMap[%S] = $objectName", channelName)
            if (basicSenderClassNameMap.containsValue(channelName)) {
                initFun.addStatement(
                    "senderChannelMap[%S] = %T($objectName.getChannel())",
                    channelName,
                    ClassName(
                        ClazzConfig.PACKAGE.CHANNEL_NAME,
                        implClazzName
                    )
                )
            }
            initFun.addStatement("")
        }

        initFun.addStatement(
            "channels = %T(senderChannelMap, receiverChannelMap)",
            ClassName(
                ClazzConfig.PACKAGE.ENGINE_NAME,
                ClazzConfig.FLUTTER_CHANNEL_NAME
            )
        )

        return initFun.build()
    }

    private fun releaseFunction(): FunSpec {
        return FunSpec.builder("release")
            .addStatement(
                "%T.releaseEngine(engineIds)",
                ClassName(
                    ClazzConfig.PACKAGE.ENGINE_NAME,
                    ClazzConfig.ENGINE_UTILS_NAME
                )
            )
            .beginControlFlow("for ((_, channel) in receiverChannelMap)")
            .addStatement("channel.release()")
            .endControlFlow()
            .addStatement("receiverChannelMap.clear()")
            .beginControlFlow("for ((_, channel) in senderChannelMap)")
            .addStatement("channel.release()")
            .endControlFlow()
            .addStatement("senderChannelMap.clear()")
            .addStatement("channels = null")
            .build()
    }
}