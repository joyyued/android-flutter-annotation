package com.joyy.ued.android_flutter_annotation.compiler.processor.manager

import com.google.auto.service.AutoService
import com.joyy.ued.android_flutter_annotation.annotation.FlutterEngine
import com.joyy.ued.android_flutter_annotation.compiler.Printer
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
 * @author: Jiang PengYong
 * @date: 2021/8/11 10:04 上午
 * @email: 56002982@qq.com
 * @des: flutter engine 处理器
 */
@AutoService(Processor::class)
class FlutterEngineProcessor : AbstractProcessor(), Printer {

    companion object {
        const val TAG = "FlutterEngineProcessor"
    }

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

        FlutterEngineUtilsProcessor(
            printer = this,
            processingEnv = processingEnv,
            roundEnv = roundEnv
        ).process()

        BaseReceiverChannelProcessor(
            printer = this,
            processingEnv = processingEnv,
            roundEnv = roundEnv
        ).process()

        BaseSenderChannelProcessor(
            printer = this,
            processingEnv = processingEnv,
            roundEnv = roundEnv
        ).process()

        FlutterManagerProcessor(
            printer = this,
            processingEnv = processingEnv,
            roundEnv = roundEnv
        ).process()

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

    override fun getMessager(): Messager? = message
}