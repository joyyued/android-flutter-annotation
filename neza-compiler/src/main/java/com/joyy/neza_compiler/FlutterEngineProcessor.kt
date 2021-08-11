package com.joyy.neza_compiler

import com.google.auto.service.AutoService
import com.joyy.neza_annotation.FlutterEngine
import com.joyy.neza_compiler.config.ClazzConfig
import com.joyy.neza_compiler.config.FlutterConfig
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import java.lang.RuntimeException
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
import javax.tools.Diagnostic

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/11 10:04 上午
 * @email: 56002982@qq.com
 * @des: flutter engine 处理器
 */
@AutoService(Processor::class)
class FlutterEngineProcessor : AbstractProcessor() {

    var filer: Filer? = null
    var elementUtils: Elements? = null
    var types: Types? = null
    var messager: Messager? = null
    var options: Map<String, String>? = null
    var sourceVersion: SourceVersion? = null
    var locale: Locale? = null

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        filer = processingEnv.filer
        elementUtils = processingEnv.elementUtils
        types = processingEnv.typeUtils
        messager = processingEnv.messager
        options = processingEnv.options
        sourceVersion = processingEnv.sourceVersion
        locale = processingEnv.locale
    }

    override fun process(annotations: Set<TypeElement?>, roundEnv: RoundEnvironment): Boolean {
        messager?.printMessage(Diagnostic.Kind.NOTE, "Flutter Engine Processor running.")
        if (annotations.isEmpty()) {
            return false
        }

        val elements = roundEnv.getElementsAnnotatedWith(FlutterEngine::class.java)
        val size = elements.size
        if (size > 1) {
            throw RuntimeException("FlutterEngine can only use once time in app.")
        } else if (size <= 0) {
            throw RuntimeException(
                "You should use FlutterEngine annotation to declare the engineId first."
            )
        }

        val element = elements.first()
        val annotation = element.getAnnotation(FlutterEngine::class.java)
        val engineId = annotation.engineId

        FlutterConfig.setEngineList(engineId)

        val contextClassName = ClassName(
            ClazzConfig.Android.CONTEXT_PACKAGE,
            ClazzConfig.Android.CONTEXT_NAME,
        )
        val engineHelperClassName = ClassName(
            ClazzConfig.ENGINE_HELPER_PACKAGE,
            ClazzConfig.ENGINE_HELPER_NAME
        )

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
            .addStatement("%T.createEngine(context, engineId)", engineHelperClassName)
            .endControlFlow()
        val initFun = initFunBuilder.build()
        val engineCreatorClazz = TypeSpec.objectBuilder(ClazzConfig.ENGINE_CREATOR_NAME)
            .addFunction(initFun)
            .build()

        val filer = filer ?: throw RuntimeException("Filer is null.Please try to run again.")

        FileSpec.get(ClazzConfig.PACKAGE.NEZA_ENGINE, engineCreatorClazz)
            .writeTo(filer)

        return true
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        val types: MutableSet<String> = LinkedHashSet()
        types.add(FlutterEngine::class.java.canonicalName)
        return types
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }
}