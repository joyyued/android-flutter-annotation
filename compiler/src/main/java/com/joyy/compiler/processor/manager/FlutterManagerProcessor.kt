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

    private val methodReceiverClassNameList = ArrayList<String>()
    private val methodReceiverChannelNameSet = HashSet<String>()
    private val methodSenderClassNameList = ArrayList<String>()
    private val methodSenderChannelNameSet = HashSet<String>()

    private val eventChannelList = ArrayList<String>()

    private val basicReceiverClassNameList = ArrayList<String>()
    private val basicReceiverChannelNameSet = HashSet<String>()
    private val basicSenderClassNameList = ArrayList<String>()
    private val basicSenderChannelNameSet = HashSet<String>()

    fun process() {
        printer.note("Flutter Manager Processor running.")

        val methodChannelPropertySpecList = handleMethodChannel(roundEnv)
        val eventChannelPropertySpecList = handleEventChannel(roundEnv)
        val basicChannelChannelPropertySpecList = handleBasicChannel(roundEnv)

        val channelsType = TypeSpec.objectBuilder(ClazzConfig.FLUTTER_CHANNEL_NAME)
            .addProperties(methodChannelPropertySpecList)
            .addProperties(eventChannelPropertySpecList)
            .addProperties(basicChannelChannelPropertySpecList)
            .build()

        val engineType = TypeSpec.objectBuilder(ClazzConfig.FLUTTER_ENGINE_NAME)
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

        val initFun = initFunction()

        val engineCreatorClazz = TypeSpec.objectBuilder(ClazzConfig.FLUTTER_MANAGER_NAME)
            .addProperty(createAllEngineProperty())
            .addProperties(createChannelMap())
            .addType(channelsType)
            .addType(engineType)
            .addFunction(initFun)
            .build()

        generatorClass(ClazzConfig.PACKAGE.ENGINE_NAME, engineCreatorClazz)
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
            .build()
    }

    private fun createChannelMap(): List<PropertySpec> {
        val list = ArrayList<PropertySpec>()

        val senderType = HashMap::class.asTypeName().parameterizedBy(
            String::class.asTypeName(),
            ClassName(
                ClazzConfig.PACKAGE.BASE_NAME,
                ClazzConfig.BASE_SENDER_CHANNEL_NAME
            )
        )
        val senderProperty = PropertySpec
            .builder(
                "senderChannelMap",
                senderType
            )
            .initializer("%T()", senderType)
            .build()
        list.add(senderProperty)

        val receiverType = HashMap::class.asTypeName().parameterizedBy(
            String::class.asTypeName(),
            ClassName(
                ClazzConfig.PACKAGE.BASE_NAME,
                ClazzConfig.BASE_RECEIVER_CHANNEL_NAME
            )
        )
        val receiverProperty = PropertySpec
            .builder(
                "receiverChannelMap",
                receiverType
            )
            .initializer("%T()", receiverType)
            .build()
        list.add(receiverProperty)

        return list
    }

    private fun handleMethodChannel(roundEnv: RoundEnvironment): ArrayList<PropertySpec> {
        val methodChannel = roundEnv.getElementsAnnotatedWith(FlutterMethodChannel::class.java)

        for (element in methodChannel) {
            if (element !is TypeElement) {
                continue
            }

            val clazzName = element.simpleName.toString()
            val annotation = element.getAnnotation(FlutterMethodChannel::class.java) ?: continue
            if (annotation.type == ChannelType.RECEIVER) {
                methodReceiverClassNameList.add(clazzName)
                methodReceiverChannelNameSet.add(annotation.channelName)
            } else {
                methodSenderClassNameList.add(clazzName)
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
                methodReceiverClassNameList.add(element.simpleName.toString())
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
            methodSenderClassNameList,
            "%T",
            "============ Method Channel ============"
        )
    }

    private fun handleEventChannel(roundEnv: RoundEnvironment): ArrayList<PropertySpec> {
        val eventChannel = roundEnv.getElementsAnnotatedWith(FlutterEventChannel::class.java)

        for (element in eventChannel) {
            if (element !is TypeElement) {
                continue
            }
            element.getAnnotation(FlutterEventChannel::class.java) ?: continue
            eventChannelList.add(element.simpleName.toString())
        }

        return assembleProperty(
            eventChannelList,
            "%T.instance",
            "============ Event Channel ============"
        )
    }

    private fun handleBasicChannel(roundEnv: RoundEnvironment): ArrayList<PropertySpec> {
        val basicChannel = roundEnv.getElementsAnnotatedWith(FlutterBasicChannel::class.java)

        for (element in basicChannel) {
            if (element !is TypeElement) {
                continue
            }

            val clazzName = element.simpleName.toString()
            val annotation = element.getAnnotation(FlutterBasicChannel::class.java) ?: continue
            if (annotation.type == ChannelType.RECEIVER) {
                basicReceiverClassNameList.add(clazzName)
                basicReceiverChannelNameSet.add(annotation.channelName)
            } else {
                basicSenderClassNameList.add(clazzName)
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
                basicReceiverClassNameList.add(element.simpleName.toString())
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
            basicSenderClassNameList,
            "%T",
            "============ Basic Channel ============"
        )
    }

    private fun assembleProperty(
        list: ArrayList<String>,
        initializer: String,
        doc: String
    ): ArrayList<PropertySpec> {
        val propertySpecList = ArrayList<PropertySpec>()
        var isFirst = true
        for (element in list) {
            val className = ClassName(
                ClazzConfig.PACKAGE.CHANNEL_NAME,
                "${element}Impl"
            )
            val proName = element.decapitalize()

            val propertySpec = PropertySpec.builder(proName, className)
                .initializer(initializer, className)

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

        for (element in methodReceiverClassNameList) {
            val name = "${element}Proxy"
            initFun.addStatement(
                "%T.instance.init(context)",
                ClassName(
                    ClazzConfig.PACKAGE.CHANNEL_NAME,
                    name
                )
            )
        }

        for (element in eventChannelList) {
            val name = "${element}Impl"
            initFun.addStatement(
                "%T.instance.init(context)",
                ClassName(
                    ClazzConfig.PACKAGE.CHANNEL_NAME,
                    name
                )
            )
        }

        for (element in basicReceiverClassNameList) {
            val name = "${element}Proxy"
            initFun.addStatement(
                "%T.instance.init(context)",
                ClassName(
                    ClazzConfig.PACKAGE.CHANNEL_NAME,
                    name
                )
            )
        }

        return initFun.build()
    }
}