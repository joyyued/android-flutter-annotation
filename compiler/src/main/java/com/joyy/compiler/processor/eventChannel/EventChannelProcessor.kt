package com.joyy.compiler.processor.eventChannel

import com.google.auto.service.AutoService
import com.joyy.annotation.FlutterEngine
import com.joyy.annotation.event.FlutterEventChannel
import com.joyy.compiler.Printer
import com.joyy.compiler.config.ClazzConfig
import com.joyy.compiler.utils.EngineHelper
import com.joyy.compiler.utils.ParamType
import com.joyy.compiler.utils.ProcessorHelper
import com.joyy.compiler.utils.TypeChangeUtils
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
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

        // 生成 XxxImpl 类名
        val generateClazzName = "${element.simpleName}Impl"

        val engineAnnotation = element.getAnnotation(FlutterEngine::class.java)
        val eventChannelAnnotation = element.getAnnotation(FlutterEventChannel::class.java)
            ?: return

        // engineId 属性
        val engineId = engineAnnotation?.engineId ?: EngineHelper.getEngineId(roundEnv)
        val engineIdProperty = PropertySpec.builder("engineId", String::class)
            .addModifiers(KModifier.PRIVATE)
            .initializer("%S", engineId)
            .build()

        // channelName 属性
        val channelName = eventChannelAnnotation.channelName
        val nameProperty = PropertySpec.builder("name", String::class)
            .addModifiers(KModifier.PRIVATE)
            .initializer("%S", channelName)
            .build()

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

        // private var channel: MethodChannel? = null
        val channelClassName = ClassName(
            ClazzConfig.Flutter.METHOD_CHANNEL_PACKAGE,
            ClazzConfig.Flutter.EVENT_CHANNEL_NAME
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
            ClazzConfig.Flutter.SINK_NAME
        )
        val sinkProperty = PropertySpec.builder(
            "eventSink",
            sinkClassName.copy(nullable = true)
        ).mutable()
            .addModifiers(KModifier.PRIVATE)
            .initializer("null")
            .build()

        // init function
        val contextClassName = ClassName(
            ClazzConfig.Android.CONTEXT_PACKAGE,
            ClazzConfig.Android.CONTEXT_NAME
        )
        val engineCacheClassName = ClassName(
            ClazzConfig.Flutter.ENGINE_PACKAGE,
            ClazzConfig.Flutter.ENGINE_CACHE_NAME
        )
        val initBlock = CodeBlock.builder()
            .beginControlFlow(
                "engine = %T.getInstance().get(engineId)?.apply",
                engineCacheClassName
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

        // getChannel function
        val getChannelFun = FunSpec.builder("getChannel")
            .addStatement("return channel")
            .build()

        // getChannelName function
        val getChannelNameFun = FunSpec.builder("getChannelName")
            .addStatement("return name")
            .build()

        // getEventSink function
        val getEventSinkFun = FunSpec.builder("getEventSink")
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

        val engineCreatorClazz = TypeSpec.classBuilder(generateClazzName)
            .addProperty(engineIdProperty)
            .addProperty(nameProperty)
            .addProperty(flutterEngineProperty)
            .addProperty(channelProperty)
            .addProperty(sinkProperty)
            .addInitializerBlock(initBlock)
            .addFunction(getChannelFun)
            .addFunction(getChannelNameFun)
            .addFunction(getEventSinkFun)
            .addFunctions(funList)
            .build()
        FileSpec.get(ClazzConfig.PACKAGE.CHANNEL_NAME, engineCreatorClazz)
            .writeTo(filer)
    }

    /**
     * 组装方法
     */
    private fun assembleFun(method: ExecutableElement): ArrayList<FunSpec> {
        val list = ArrayList<FunSpec>()

        val methodName = method.simpleName.toString()
        val methodParameters = method.parameters
        val paramType = ProcessorHelper.checkParam(this, method, methodParameters)

        when (paramType) {
            ParamType.ORIGIN -> list.add(
                createSingleParamFun(
                    methodName,
                    methodParameters
                )
            )
            ParamType.MAP -> list.addAll(
                createMultiParamsFun(
                    methodName,
                    methodParameters
                )
            )
        }

        if (!existMethods.contains(methodName)) {
            val errorFun = FunSpec.builder(methodName)
                .addParameter("errorCode", String::class)
                .addParameter("errorMessage", String::class)
                .addParameter("errorDetails", Any::class)
                .addParameter(
                    ParameterSpec.builder(
                        "type",
                        ClassName(
                            ClazzConfig.EVENT_CHANNEL_SENDER_TYPE_PACKAGE,
                            ClazzConfig.EVENT_CHANNEL_SENDER_ERROR_TYPE_NAME
                        )
                    ).defaultValue("EventChannelSenderErrorType.ERROR")
                        .build()
                )
                .addStatement("eventSink?.error(errorCode, errorMessage, errorDetails)")
                .build()
            list.add(errorFun)
            val eosFun = FunSpec.builder(methodName)
                .addParameter(
                    ParameterSpec.builder(
                        "type",
                        ClassName(
                            ClazzConfig.EVENT_CHANNEL_SENDER_TYPE_PACKAGE,
                            ClazzConfig.EVENT_CHANNEL_SENDER_EOS_TYPE_NAME
                        )
                    ).defaultValue("EventChannelSenderEOSType.EOS")
                        .build()
                ).addStatement("eventSink?.endOfStream()")
                .build()
            list.add(eosFun)
        }

        existMethods.add(methodName)

        return list
    }

    private fun createSingleParamFun(
        methodName: String,
        params: List<VariableElement>
    ): FunSpec {

        val parameterList = ArrayList<ParameterSpec>()
        for (param in params) {
            var type = TypeChangeUtils.change(this, param.asType())
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
        }

        var log = ""
        val paramName = when {
            params.isEmpty() -> {
                "\"\""
            }
            params.size == 1 -> {
                params[0].simpleName
            }
            else -> {
                log = "You use @Param annotation on multi parameters function." +
                        "This caused only the first parameter will be used."
                warning("$log [ $methodName ] ")
                params[0].simpleName
            }
        }

        val function = FunSpec.builder(methodName)
            .addParameters(parameterList)

        if (log.isNotEmpty()) {
            function.addStatement(
                "%T.e(%S, %S)",
                ClassName(
                    ClazzConfig.Android.ANDROID_UTIL_PACKAGE,
                    ClazzConfig.Android.ANDROID_LOG_NAME
                ),
                ClazzConfig.PROJECT_NAME,
                log
            )
        }

        function.addStatement("eventSink?.success($paramName)")
        return function.build()
    }

    private fun createMultiParamsFun(
        methodName: String,
        params: List<VariableElement>
    ): ArrayList<FunSpec> {
        val list = ArrayList<FunSpec>()
        val orgFun = FunSpec.builder(methodName)
            .addStatement("val params = HashMap<String, Any?>()")

        val parameterList = ArrayList<ParameterSpec>()
        for (param in params) {
            var type = TypeChangeUtils.change(this, param.asType())
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
                .addStatement("eventSink?.success(params)")
                .build()
        )

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