package com.joyy.neza_compiler.processor.manager

import com.joyy.neza_annotation.basic.FlutterBasicChannel
import com.joyy.neza_annotation.event.FlutterEventChannel
import com.joyy.neza_annotation.method.FlutterMethodChannel
import com.joyy.neza_annotation.model.ChannelType
import com.joyy.neza_compiler.Printer
import com.joyy.neza_compiler.config.ClazzConfig
import com.joyy.neza_compiler.utils.EngineHelper
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.util.Locale
import javax.annotation.processing.Filer
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/11 10:04 上午
 * @email: 56002982@qq.com
 * @des: flutter manager 处理器
 */
class FlutterManagerProcessor(private val filer: Filer, private val printer: Printer) {

    private val methodChannelList = ArrayList<String>()
    private val eventChannelList = ArrayList<String>()
    private val basicChannelList = ArrayList<String>()

    fun process(roundEnv: RoundEnvironment) {
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
                            ClazzConfig.Flutter.ENGINE_NAME,
                        ).copy(nullable = true)
                    )
                    .addStatement(
                        "return %T.getFlutterEngine(engineId)",
                        ClassName(
                            ClazzConfig.ENGINE_HELPER_PACKAGE,
                            ClazzConfig.ENGINE_HELPER_NAME,
                        )
                    )
                    .build()
            )
            .build()

        val initFun = handleForInit()

        val engineCreatorClazz = TypeSpec.objectBuilder(ClazzConfig.FLUTTER_MANAGER_NAME)
            .addType(channelsType)
            .addType(engineType)
            .addFunction(initFun)
            .build()

        FileSpec.get(ClazzConfig.PACKAGE.FLUTTER_MANAGER, engineCreatorClazz)
            .writeTo(filer)
    }

    private fun handleMethodChannel(roundEnv: RoundEnvironment): ArrayList<PropertySpec> {
        val methodChannel = roundEnv.getElementsAnnotatedWith(FlutterMethodChannel::class.java)

        val channelSet: HashSet<String> = HashSet()

        for (element in methodChannel) {
            if (element !is TypeElement) {
                continue
            }

            val clazzName = element.simpleName.toString()
            val annotation = element.getAnnotation(FlutterMethodChannel::class.java) ?: continue
            if (annotation.type == ChannelType.RECEIVER) {
                methodChannelList.add(clazzName)
                channelSet.add(annotation.channelName)
            }
        }

        for (element in methodChannel) {
            if (element !is TypeElement) {
                continue
            }

            val clazzName = element.simpleName.toString()
            val annotation = element.getAnnotation(FlutterMethodChannel::class.java) ?: continue
            if (annotation.type == ChannelType.SENDER
                && !channelSet.contains(annotation.channelName)
            ) {
                methodChannelList.add(clazzName)
                channelSet.add(annotation.channelName)
            }
        }

        return assembleProperty(
            methodChannelList,
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

        val channelSet: HashSet<String> = HashSet()

        for (element in basicChannel) {
            if (element !is TypeElement) {
                continue
            }

            val clazzName = element.simpleName.toString()
            val annotation = element.getAnnotation(FlutterBasicChannel::class.java) ?: continue
            if (annotation.type == ChannelType.RECEIVER) {
                basicChannelList.add(clazzName)
                channelSet.add(annotation.channelName)
            }
        }

        for (element in basicChannel) {
            if (element !is TypeElement) {
                continue
            }

            val clazzName = element.simpleName.toString()
            val annotation = element.getAnnotation(FlutterBasicChannel::class.java) ?: continue
            if (annotation.type == ChannelType.SENDER
                && !channelSet.contains(annotation.channelName)
            ) {
                basicChannelList.add(clazzName)
                channelSet.add(annotation.channelName)
            }
        }

        return assembleProperty(
            basicChannelList,
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
                ClazzConfig.PACKAGE.NEZA_CHANNEL,
                "${element}Impl"
            )
            val proName = element.replaceFirstChar { it.lowercase(Locale.getDefault()) }

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

    private fun handleForInit(): FunSpec {
        val initFun = FunSpec.builder(ClazzConfig.FLUTTER_INIT_NAME)
            .addParameter(
                "context",
                ClassName(
                    ClazzConfig.Android.CONTEXT_PACKAGE,
                    ClazzConfig.Android.CONTEXT_NAME,
                )
            )

        initFun.addStatement(
            "%T.init(context)",
            ClassName(
                ClazzConfig.PACKAGE.NEZA_ENGINE,
                ClazzConfig.ENGINE_CREATOR_NAME
            )
        )

        for (element in methodChannelList) {
            val name = "${element}Proxy"
            initFun.addStatement(
                "%T.instance.init(context)",
                ClassName(
                    ClazzConfig.PACKAGE.NEZA_CHANNEL,
                    name
                )
            )
        }

        for (element in eventChannelList) {
            val name = "${element}Impl"
            initFun.addStatement(
                "%T.instance.init(context)",
                ClassName(
                    ClazzConfig.PACKAGE.NEZA_CHANNEL,
                    name
                )
            )
        }

        for (element in basicChannelList) {
            val name = "${element}Proxy"
            initFun.addStatement(
                "%T.instance.init(context)",
                ClassName(
                    ClazzConfig.PACKAGE.NEZA_CHANNEL,
                    name
                )
            )
        }

        return initFun.build()
    }
}