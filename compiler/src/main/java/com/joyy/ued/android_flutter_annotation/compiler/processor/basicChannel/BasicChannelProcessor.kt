package com.joyy.ued.android_flutter_annotation.compiler.processor.basicChannel

import com.google.auto.service.AutoService
import com.joyy.ued.android_flutter_annotation.annotation.basic.FlutterBasicChannel
import com.joyy.ued.android_flutter_annotation.annotation.model.ChannelType
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
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * @author: Jiang PengYong
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
        note("Basic Channel Processor running .")

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
        receiverProcessor = ReceiverProcessor(this, processingEnv, roundEnv)
        senderProcessor = SenderProcessor(this, processingEnv, roundEnv)

        if (annotations.isEmpty()) {
            return false
        }

        val elements = roundEnv.getElementsAnnotatedWith(FlutterBasicChannel::class.java)
        if (elements.isEmpty()) {
            return true
        }

        // 接收者
        val receiver = ArrayList<Element>()
        // 发送者
        val sender = ArrayList<Element>()
        elements.filterNotNull().forEach { element ->
            val annotation = element.getAnnotation(FlutterBasicChannel::class.java)
            when (annotation.type) {
                ChannelType.RECEIVER -> receiver.add(element)
                ChannelType.SENDER -> sender.add(element)
            }
        }

        val channelReceiverMap = HashMap<String, ChannelInfo>()

        // ============================ 生成接收者 ===============================
        receiver.forEach { element ->
            if (element !is TypeElement) {
                return@forEach
            }
            receiverProcessor?.handle(element, channelReceiverMap)
        }

        // ============================ 生成发送者 ================================
        sender.forEach { element ->
            if (element !is TypeElement) {
                return@forEach
            }

            senderProcessor?.handle(element, channelReceiverMap)
        }

        return true
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        val types: MutableSet<String> = LinkedHashSet()
        types.add(FlutterBasicChannel::class.java.canonicalName)
        return types
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getMessager(): Messager? = message
}