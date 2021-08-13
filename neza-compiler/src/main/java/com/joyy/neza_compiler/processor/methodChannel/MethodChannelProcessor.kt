package com.joyy.neza_compiler.processor.methodChannel

import com.google.auto.service.AutoService
import javax.annotation.processing.Filer
import javax.lang.model.SourceVersion
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.annotation.processing.RoundEnvironment
import com.joyy.neza_annotation.method.FlutterMethodChannel
import com.joyy.neza_annotation.model.MethodChannelType
import com.joyy.neza_compiler.Printer
import com.squareup.kotlinpoet.ClassName
import java.util.LinkedHashSet
import java.util.Locale
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Messager
import javax.annotation.processing.Processor
import javax.lang.model.element.Element
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/11 1:58 下午
 * @email: 56002982@qq.com
 * @des: Method Channel 处理器
 */
@AutoService(Processor::class)
class MethodChannelProcessor : AbstractProcessor(), Printer {

    private var filer: Filer? = null
    private var elementUtils: Elements? = null
    private var types: Types? = null
    private var message: Messager? = null
    private var options: Map<String, String>? = null
    private var sourceVersion: SourceVersion? = null
    private var locale: Locale? = null

    private var receiverProcessor: ReceiverProcessor? = null
    private var senderProcessor: SenderProcessor? = null

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
        receiverProcessor = ReceiverProcessor(filer, this)
        senderProcessor = SenderProcessor(filer, this)

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
                MethodChannelType.RECEIVER -> receiver.add(element)
                MethodChannelType.SENDER -> sender.add(element)
            }
        }

        val channelReceiverMap = HashMap<String, ClassName>()

        // ============================ 生成接收者 ===============================
        receiver.forEach { element ->
            receiverProcessor?.handle(roundEnv, element, channelReceiverMap)
        }

        // ============================ 生成发送者 ================================
        sender.forEach { element ->
            senderProcessor?.handle(roundEnv, element, channelReceiverMap)
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