//package com.joyy.neza_compiler
//
//import com.google.auto.service.AutoService
//import javax.annotation.processing.Filer
//import javax.lang.model.SourceVersion
//import javax.annotation.processing.ProcessingEnvironment
//import javax.lang.model.element.TypeElement
//import javax.annotation.processing.RoundEnvironment
//import com.joyy.neza_annotation.method.FlutterMethodChannel
//import java.util.LinkedHashSet
//import java.util.Locale
//import javax.annotation.processing.AbstractProcessor
//import javax.annotation.processing.Messager
//import javax.annotation.processing.Processor
//import javax.lang.model.util.Elements
//import javax.lang.model.util.Types
//
//@AutoService(Processor::class)
//class MethodChannelProcessor : AbstractProcessor() {
//    var filer: Filer? = null
//    var elementUtils: Elements? = null
//    var types: Types? = null
//    var messager: Messager? = null
//    var options: Map<String, String>? = null
//    var sourceVersion: SourceVersion? = null
//    var locale: Locale? = null
//    @Synchronized
//    override fun init(processingEnv: ProcessingEnvironment) {
//        super.init(processingEnv)
//        filer = processingEnv.filer
//        elementUtils = processingEnv.elementUtils
//        types = processingEnv.typeUtils
//        messager = processingEnv.messager
//        options = processingEnv.options
//        sourceVersion = processingEnv.sourceVersion
//        locale = processingEnv.locale
//    }
//
//    override fun process(annotations: Set<TypeElement?>, roundEnv: RoundEnvironment): Boolean {
//        return if (annotations.isEmpty()) {
//            false
//        } else true
//    }
//
//    /**
//     * 返回需要被处理的注解的类型名称的集合
//     * 格式是包名+类名，例如 com.zinc.router_annotation.Router
//     *
//     * @return
//     */
//    override fun getSupportedAnnotationTypes(): Set<String> {
//        val types: MutableSet<String> = LinkedHashSet()
//        types.add(FlutterMethodChannel::class.java.canonicalName)
//        return types
//    }
//
//    override fun getSupportedSourceVersion(): SourceVersion {
//        return SourceVersion.latestSupported()
//    }
//}