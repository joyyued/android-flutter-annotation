package com.joyy.neza_compiler.eventChannel

import com.google.auto.service.AutoService
import com.joyy.neza_annotation.FlutterEngine
import com.joyy.neza_annotation.event.FlutterEventChannel
import com.joyy.neza_compiler.Printer
import com.joyy.neza_compiler.config.ClazzConfig
import com.joyy.neza_compiler.utils.EngineHelper
import com.joyy.neza_compiler.utils.TypeChangeUtils
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import org.jetbrains.annotations.Nullable
import java.util.LinkedHashSet
import java.util.Locale
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/11 1:58 下午
 * @email: 56002982@qq.com
 * @des: Event Channel 处理器
 */
@AutoService(Processor::class)
class EventChannelProcessor : AbstractProcessor(), Printer {

    private var filer: Filer? = null
    private var elementUtils: Elements? = null
    private var types: Types? = null
    private var message: Messager? = null
    private var options: Map<String, String>? = null
    private var sourceVersion: SourceVersion? = null
    private var locale: Locale? = null

    private var existMethods: HashSet<String> = HashSet()
    private var existMapMethods: HashSet<String> = HashSet()

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        filer = processingEnv.filer
        elementUtils = processingEnv.elementUtils
        types = processingEnv.typeUtils
        message = processingEnv.messager
        options = processingEnv.options
        sourceVersion = processingEnv.sourceVersion
        locale = processingEnv.locale
    }

    override fun process(annotations: Set<TypeElement?>, roundEnv: RoundEnvironment): Boolean {
        note("Event Channel Processor running .")

        val filer = filer
        if (filer == null) {
            error("Filer is null.Please try to run again.")
            return true
        }

        val elements = roundEnv.getElementsAnnotatedWith(FlutterEventChannel::class.java)

        for (element in elements) {
            if (element !is TypeElement) {
                continue
            }
            handleEventChannel(roundEnv, element, filer)
        }

        return true
    }

    private fun handleEventChannel(
        roundEnv: RoundEnvironment,
        element: TypeElement,
        filer: Filer
    ) {
        if (element.kind != ElementKind.INTERFACE) {
            error("@FlutterEventChannel only can use on Interface.")
            return
        }

        val generateClazzName = "${element.simpleName}Impl"

        val engineAnnotation = element.getAnnotation(FlutterEngine::class.java)
        val engineId = engineAnnotation?.engineId ?: EngineHelper.getEngineId(roundEnv)
        val engineIdProperty = PropertySpec.builder("engineId", String::class)
            .addModifiers(KModifier.PRIVATE)
            .initializer("%S", engineId)
            .build()

        val eventChannelAnnotation = element.getAnnotation(FlutterEventChannel::class.java)
            ?: return
        val channelName = eventChannelAnnotation.channelName
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
            ClazzConfig.Flutter.EVENT_CHANNEL_NAME,
        )
        val channelProperty = PropertySpec.builder(
            "channel",
            channelClassName.copy(nullable = true)
        ).mutable()
            .addModifiers(KModifier.PRIVATE)
            .initializer("null")
            .build()

        // private var eventSink: EventChannel.EventSink? = null
        val sinkClassName = ClassName(
            ClazzConfig.Flutter.METHOD_CHANNEL_PACKAGE,
            ClazzConfig.Flutter.SINK_NAME,
        )
        val sinkProperty = PropertySpec.builder(
            "eventSink",
            sinkClassName.copy(nullable = true)
        ).mutable()
            .addModifiers(KModifier.PRIVATE)
            .initializer("null")
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
            .addStatement("channel = EventChannel(")
            .addStatement("  dartExecutor.binaryMessenger,")
            .addStatement("  name")
            .addStatement(")")
            .addStatement("channel?.setStreamHandler(")
            .addStatement("  object : EventChannel.StreamHandler {")
            .addStatement("    override fun onListen(")
            .addStatement("      arguments: Any?,")
            .addStatement("      events: EventChannel.EventSink")
            .addStatement("    ) {")
            .addStatement("      eventSink = events")
            .addStatement("    }")
            .addStatement("    override fun onCancel(arguments: Any?) {")
            .addStatement("      eventSink = null")
            .addStatement("    }")
            .addStatement("  })")
            .endControlFlow()
            .build()

        val getChannelFun = FunSpec.builder("getChannel")
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return channel")
            .build()

        val getChannelNameFun = FunSpec.builder("getChannelName")
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return name")
            .build()

        val getEventSinkFun = FunSpec.builder("getEventSink")
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return eventSink")
            .build()

        val funList = ArrayList<FunSpec>()
        val enclosedElements = element.enclosedElements
        for (method in enclosedElements) {
            if (method !is ExecutableElement) {
                continue
            }
            funList.addAll(assembleFun(method))
        }


        val eventChannelClassName = ClassName(
            ClazzConfig.Channel.CHANNEL_PACKAGE,
            ClazzConfig.Channel.EVENT_CHANNEL_NAME
        )

        val engineCreatorClazz = TypeSpec.classBuilder(generateClazzName)
            .addSuperinterface(element.asType().asTypeName())
            .addSuperinterface(eventChannelClassName)
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
            .addProperty(sinkProperty)
            .addFunction(initFun)
            .addFunction(getChannelFun)
            .addFunction(getChannelNameFun)
            .addFunction(getEventSinkFun)
            .addFunctions(funList)
            .build()
        FileSpec.get(ClazzConfig.PACKAGE.NEZA_CHANNEL, engineCreatorClazz)
            .writeTo(filer)
    }

    private fun assembleFun(method: ExecutableElement): ArrayList<FunSpec> {
        val list = ArrayList<FunSpec>()

        val methodName = method.simpleName.toString()

        when {
            method.parameters.size <= 0 -> list.add(createNoneParamFun(methodName))
            method.parameters.size == 1 -> list.add(
                createSingleParamFun(
                    methodName,
                    method.parameters[0]
                )
            )
            else -> list.addAll(createMultiParamsFun(methodName, method.parameters))
        }

        if (!existMethods.contains(methodName)) {
            val errorFun = FunSpec.builder(methodName)
                .addParameter(
                    ParameterSpec.builder(
                        "type",
                        ClassName(
                            ClazzConfig.EVENT_CHANNEL_SENDER_TYPE_PACKAGE,
                            ClazzConfig.EVENT_CHANNEL_SENDER_TYPE_NAME,
                        )
                    ).defaultValue("EventChannelSenderType.ERROR")
                        .build()
                ).addParameter("errorCode", String::class)
                .addParameter("errorMessage", String::class)
                .addParameter("errorDetails", Any::class)
                .addStatement("eventSink?.error(errorCode, errorMessage, errorDetails)")
                .build()
            list.add(errorFun)
            val eosFun = FunSpec.builder(methodName)
                .addParameter(
                    ParameterSpec.builder(
                        "type",
                        ClassName(
                            ClazzConfig.EVENT_CHANNEL_SENDER_TYPE_PACKAGE,
                            ClazzConfig.EVENT_CHANNEL_SENDER_TYPE_NAME,
                        )
                    ).defaultValue("EventChannelSenderType.EOS")
                        .build()
                ).addStatement("eventSink?.endOfStream()")
                .build()
            list.add(eosFun)
        }

        existMethods.add(methodName)

        return list
    }

    private fun createNoneParamFun(methodName: String): FunSpec {
        return FunSpec.builder(methodName)
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("eventSink?.success(Any())")
            .build()
    }

    private fun createSingleParamFun(
        methodName: String,
        param: VariableElement
    ): FunSpec {
        var type = param.asType().asTypeName()
        type = TypeChangeUtils.change(type)

        return FunSpec.builder(methodName)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter(
                ParameterSpec.builder(
                    param.simpleName.toString(),
                    type
                ).build()
            )
            .addStatement("eventSink?.success(${param.simpleName.toString()})")
            .build()
    }

    private fun createMultiParamsFun(
        methodName: String,
        params: List<VariableElement>
    ): ArrayList<FunSpec> {
        val list = ArrayList<FunSpec>()
        val orgFun = FunSpec.builder(methodName)
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("val params = HashMap<String, Any?>()")

        val parameterList = ArrayList<ParameterSpec>()
        for (param in params) {
            var type = param.asType().asTypeName()
            type = TypeChangeUtils.change(type)
            val nullableAnnotation = param.getAnnotation(Nullable::class.java)
            if (nullableAnnotation != null) {
                type = type.copy(nullable = true)
            }
            parameterList.add(
                ParameterSpec.builder(
                    param.simpleName.toString(),
                    type
                ).build()
            )
            orgFun.addStatement("params[%S] = $param", param.simpleName.toString())
        }

        list.add(
            orgFun.addParameters(parameterList)
                .addStatement("$methodName(params)")
                .build()
        )

        if (!existMapMethods.contains(methodName)) {
            val paramFun = FunSpec.builder(methodName)
                .addParameter(
                    "params", HashMap::class.asClassName().parameterizedBy(
                        String::class.asTypeName(),
                        Any::class.asClassName().copy(nullable = true)
                    )
                )
                .addStatement("eventSink?.success(params)")
                .build()
            list.add(paramFun)
        }

        existMapMethods.add(methodName)

        return list
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        val types: MutableSet<String> = LinkedHashSet()
        types.add(FlutterEventChannel::class.java.canonicalName)
        return types
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getMessager(): Messager? = message
}