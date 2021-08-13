package com.joyy.neza_compiler.processor.basicChannel

import com.google.auto.service.AutoService
import com.joyy.neza_annotation.method.FlutterMethodChannel
import com.joyy.neza_annotation.model.ChannelType
import com.joyy.neza_compiler.Printer
import java.util.LinkedHashSet
import java.util.Locale
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/11 1:58 下午
 * @email: 56002982@qq.com
 * @des: Basic Channel 处理器
 */
@AutoService(Processor::class)
class BasicChannelProcessor : AbstractProcessor(), Printer {

    private var filer: Filer? = null
    private var elementUtils: Elements? = null
    private var types: Types? = null
    private var message: Messager? = null
    private var options: Map<String, String>? = null
    private var sourceVersion: SourceVersion? = null
    private var locale: Locale? = null

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
        note("Method Channel Processor running .")

        val filer = filer
        if (filer == null) {
            error("Filer is null.Please try to run again.")
            return true
        }

        if (annotations.isEmpty()) {
            return false
        }

        val elements = roundEnv.getElementsAnnotatedWith(FlutterMethodChannel::class.java)
        if (elements.isEmpty()) {
            return true
        }

        // 接收者
        val receiver = ArrayList<Element>()
        // 发送者
        val sender = ArrayList<Element>()
        elements.filterNotNull().forEach { element ->
            val annotation = element.getAnnotation(FlutterMethodChannel::class.java)
            when (annotation.type) {
                ChannelType.RECEIVER -> receiver.add(element)
                ChannelType.SENDER -> sender.add(element)
            }
        }


        return true
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        val types: MutableSet<String> = LinkedHashSet()
        types.add(FlutterMethodChannel::class.java.canonicalName)
        return types
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getMessager(): Messager? = message
}