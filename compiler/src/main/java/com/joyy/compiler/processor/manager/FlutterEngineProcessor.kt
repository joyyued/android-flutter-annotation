package com.joyy.compiler.processor.manager

import com.google.auto.service.AutoService
import com.joyy.annotation.FlutterEngine
import com.joyy.compiler.Printer
import com.joyy.compiler.config.ClazzConfig
import com.joyy.compiler.utils.EngineHelper
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import java.util.LinkedHashSet
import java.util.Locale
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/11 10:04 上午
 * @email: 56002982@qq.com
 * @des: flutter engine 处理器
 */
@AutoService(Processor::class)
class FlutterEngineProcessor : AbstractProcessor(), Printer {

    companion object {
        const val TAG = "FlutterEngineProcessor"
    }

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
    )
    private val dartExecutorClassName = ClassName(
        ClazzConfig.Flutter.DART_EXECUTOR_PACKAGE,
        ClazzConfig.Flutter.DART_EXECUTOR_NAME
    )

    var filer: Filer? = null
    var elementUtils: Elements? = null
    var types: Types? = null
    var message: Messager? = null
    var options: Map<String, String>? = null
    var sourceVersion: SourceVersion? = null
    var locale: Locale? = null

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
        note("Flutter Engine Processor running.")
        if (annotations.isEmpty()) {
            return false
        }

        val element = EngineHelper.getFlutterEngineElements(roundEnv).first()
        val annotation = element.getAnnotation(FlutterEngine::class.java)
        val engineId = annotation.engineId

        // ArrayList<String>
        val arrayList = ClassName("kotlin.collections", "ArrayList")
        val arrayListOfString = arrayList.parameterizedBy(String::class.asTypeName())

        // 可扩展
        val engineIds = ArrayList<String>()
        engineIds.add(engineId)

        val initFunBuilder = FunSpec.builder("init")
            .addParameter("context", contextClassName)
            .addStatement("val engineIds = %T()", arrayListOfString)
        engineIds.forEach { engineId ->
            initFunBuilder.addStatement("engineIds.add(%S)", engineId)
        }

        initFunBuilder.beginControlFlow("for (engineId in engineIds) {")
            .addStatement("createEngine(context, engineId)")
            .endControlFlow()
        val initFun = initFunBuilder.build()
        val engineCreatorClazz = TypeSpec.objectBuilder(ClazzConfig.ENGINE_CREATOR_NAME)
            .addFunction(initFun)
            .addFunction(createEngineFunction())
            .build()

        val filer = filer
        if (filer == null) {
            error("Filer is null.Please try to run again.")
            return true
        }
        val elementUtils = elementUtils
        if (elementUtils == null) {
            error("Element utils is null.Please try to run again.")
            return true
        }
        val typeUtils = types
        if (typeUtils == null) {
            error("Type utils is null.Please try to run again.")
            return true
        }

        FileSpec.get(ClazzConfig.PACKAGE.ENGINE_NAME, engineCreatorClazz)
            .writeTo(filer)

        FlutterManagerProcessor(
            printer = this,
            processingEnv = processingEnv
        ).process(roundEnv)

        return true
    }

    private fun createEngineFunction(): FunSpec {
        return FunSpec.builder("createEngine")
            .addParameter("context", contextClassName)
            .addParameter("engineId", String::class)
            .returns(engineClassName)
            .addStatement(
                "var engine = %T.getInstance().get(engineId)",
                engineCacheClassName
            )
            .beginControlFlow("if (engine == null)")
            .beginControlFlow(
                "engine = %T(context).apply",
                engineClassName
            )
            .addStatement(
                "dartExecutor.executeDartEntrypoint(%T.DartEntrypoint.createDefault())",
                dartExecutorClassName
            )
            .endControlFlow()
            .addStatement("%T.getInstance().put(engineId, engine)", engineCacheClassName)
            .endControlFlow()
            .addStatement("return engine")
            .build()
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        val types: MutableSet<String> = LinkedHashSet()
        types.add(FlutterEngine::class.java.canonicalName)
        return types
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getMessager(): Messager? = message
}